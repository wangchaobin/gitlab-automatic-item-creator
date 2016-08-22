/*
 * Copyright wangchaobin 2016
 */
package com.wang.app.mirana;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.gitlab.api.GitlabAPI;
import org.jdom.JDOMException;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import com.wang.app.mirana.model.GitlabEventModel;
import com.wang.app.mirana.util.GitlabUtil;
import com.wang.app.mirana.util.JenkinsItemUtil;
import com.wang.app.mirana.util.JenkinsUtil;

import hudson.Extension;
import hudson.model.Item;
import hudson.model.TopLevelItem;
import hudson.model.UnprotectedRootAction;
import hudson.security.AuthorizationStrategy;
import hudson.security.csrf.CrumbExclusion;
import hudson.util.HttpResponses;
import jenkins.model.Jenkins;

/**
 * Created by Wang on 20/1/2016.
 */
@Extension
public class GitLabSystemHook implements UnprotectedRootAction {

	private static final Logger LOGGER = Logger.getLogger(GitLabSystemHook.class.getName());

	public static final String SYSTEM_HOOK_URL = "systemhook";

	public static final String HOOK_MESSAGE = "newproject";

	private static String EVENT_CREATE = "project_create";

	private static String EVENT_DESTROY = "project_destroy";

	private static int EVENT_TYPE_CREATE = 0;

	private static int EVENT_TYPE_DESTROY = 1;

	private static int EVENT_TYPE_OTHER = -1;

	@Override
	public String getIconFileName() {
		return null;
	}

	@Override
	public String getDisplayName() {
		return null;
	}

	@Override
	public String getUrlName() {
		return SYSTEM_HOOK_URL;
	}

	public void getDynamic(final String message, final StaplerRequest req, StaplerResponse res) {
		LOGGER.log(Level.INFO, "SystemHook called with url: {0}", req.getRequestURI());
		if (!message.equals(HOOK_MESSAGE)) {
			throw HttpResponses.notFound();
		}

		String hookMessage = null;
		try {
			hookMessage = getHookMessage(req);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "SystemHook get hook content failed. {0}", e.getMessage());
			throw HttpResponses.error(500, "Error happens when analyzing request.");
		}

		// Only dealing with creating or deleting project.
		if (getActionType(hookMessage) < 0) {
			LOGGER.log(Level.INFO, "SystemHook only deal with project creating or deleting, others skip.");
			throw HttpResponses.ok();
		}

		GitlabEventModel gem = new GitlabEventModel(hookMessage);
		if (gem.getEventName().equals(EVENT_CREATE)) {
			try {
				createItemOnEvent(gem);
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "SystemHook error happens when creating job automatically. {0}",
						e.getMessage());
				throw HttpResponses.error(500, "Error happens when creating job automatically.");
			}
		}

		if (gem.getEventName().equals(EVENT_DESTROY)) {
			try {
				destroyItemOnEvent(gem);
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "SystemHook error happens when destroying job automatically. {0}",
						e.getMessage());
				throw HttpResponses.error(500, "Error happens when destroying job automatically.");
			}
		}

		throw HttpResponses.ok();
	}

	private String getHookMessage(StaplerRequest req) throws IOException {
		String result = null;
		StringWriter writer = new StringWriter();
		try {
			IOUtils.copy(req.getInputStream(), writer, "UTF-8");
			result = writer.toString();
			LOGGER.log(Level.INFO, "SystemHook content: {0}", result);
		} catch (IOException e) {
			throw e;
		} finally {
			writer.close();
		}
		return result;
	}

	private int getActionType(String message) {
		if (message.contains("project_create"))
			return EVENT_TYPE_CREATE;
		if (message.contains("project_destroy"))
			return EVENT_TYPE_DESTROY;
		return EVENT_TYPE_OTHER;
	}

	private synchronized void createItemOnEvent(GitlabEventModel gem) throws IOException, JDOMException {
		LOGGER.log(Level.INFO, "SystemHook createItemOnEvent start, project name is : {0}.", gem.getProjectName());
		String proName = gem.getProjectName();
		// Use Gitlab Project ID to name Jenkins job.
		String jobName = gem.getProjectId();
		GitlabAPI api = GitlabUtil.instance();
		Jenkins jenkins = Jenkins.getInstance();
		AuthorizationStrategy as = jenkins.getAuthorizationStrategy();
		jenkins.setAuthorizationStrategy(null);
		if (!JenkinsUtil.isItemExisting(jobName)) {
			// Set Gitlab Repository URL in config.xml of Item in Jenkins Server.
			Map<String, String> urlmap = GitlabUtil.getRepositoryUrl(api, proName);
			String repositoryUrl = urlmap.get("http");
			Item templateItem = jenkins.getItem(JenkinsUtil.TEMPLATE_MAVEN_CONFIG);
			if(templateItem == null ){
				LOGGER.log(Level.SEVERE, "SystemHook template Job {0} not exists, Please contact to the Administrator.", JenkinsUtil.TEMPLATE_MAVEN_CONFIG);
				throw new IOException("Template not exists.");
			}
			File config = JenkinsItemUtil.getConfigAfterModifyProperty(templateItem, repositoryUrl);
			jenkins.createProjectFromXML(jobName, new FileInputStream(config));
			config.delete();

			/*
			 * Make up CI Service URL. http://{JENKINS_HOST}/project/{projectId}
			 */
			String hookUrl = Jenkins.getInstance().getRootUrl();
			if (!hookUrl.trim().endsWith("/")) {
				hookUrl = hookUrl.trim() + "/";
			}
			hookUrl += ("project/" + jobName);
			LOGGER.log(Level.INFO, "SystemHook hookUrl : {0} .", hookUrl);
			// Set WebHook to project in Gitlab Server.
			GitlabUtil.addWebHookIfNotExisted(api, proName, hookUrl);
		}
		jenkins.setAuthorizationStrategy(as);
		LOGGER.log(Level.INFO, "SystemHook create Item On Event done.");
	}

	private synchronized void destroyItemOnEvent(GitlabEventModel gem) throws IllegalArgumentException, IOException {
		String jobName = gem.getProjectId();
		LOGGER.log(Level.INFO, "SystemHook destroy Item On Event start. Job name is {0}", jobName);
		File path = null;
		if (JenkinsUtil.isItemExisting(jobName)) {
			Jenkins jenkins = Jenkins.getInstance();
			AuthorizationStrategy as = jenkins.getAuthorizationStrategy();
			jenkins.setAuthorizationStrategy(null);
			Item target = jenkins.getItemByFullName(jobName);
			path = target.getRootDir();
			jenkins.remove((TopLevelItem) target);
			jenkins.setAuthorizationStrategy(as);
		}
		if (path != null && path.isDirectory()) {
			deleteDir(path);
		}
		LOGGER.log(Level.INFO, "SystemHook destroy Item On Event done. Job name is {0}", jobName);
	}

	@Extension
	public static class GitlabSystemHookCrumbExclusion extends CrumbExclusion {

		@Override
		public boolean process(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
				throws IOException, ServletException {
			String pathInfo = req.getPathInfo();
			if (pathInfo != null && pathInfo.startsWith(getExclusionPath())) {
				chain.doFilter(req, resp);
				return true;
			}
			return false;
		}

		private String getExclusionPath() {
			return '/' + SYSTEM_HOOK_URL + '/';
		}
	}

	private boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		return dir.delete();
	}

}
