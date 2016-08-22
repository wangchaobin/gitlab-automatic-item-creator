/*
 * Copyright wangchaobin 2016
 */
package com.wang.app.mirana.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gitlab.api.GitlabAPI;
import org.gitlab.api.models.GitlabProject;
import org.gitlab.api.models.GitlabProjectHook;
import org.gitlab.api.models.GitlabSystemHook;

import com.dabsquared.gitlabjenkins.GitLabPushTrigger;

/**
 * Created by Wang on 20/1/2016.
 */
public class GitlabUtil {

	private static final Logger LOGGER = Logger.getLogger(GitlabUtil.class.getName());

	public static GitlabAPI instance() {
		GitlabAPI api;
		String token = GitLabPushTrigger.getDesc().getGitlabApiToken();
		String url = GitLabPushTrigger.getDesc().getGitlabHostUrl();
		boolean ignoreCertificateErrors = GitLabPushTrigger.getDesc().getIgnoreCertificateErrors();
		LOGGER.log(Level.FINE, "Connecting to Gitlab server ({0})", url);
		api = GitlabAPI.connect(url, token);
		api.ignoreCertificateErrors(ignoreCertificateErrors);
		return api;
	}

	public static List<GitlabSystemHook> getGitlabSystemHook(GitlabAPI api) throws IOException {
		return api.getSystemHooks();
	}

	public static List<GitlabProjectHook> getGitlabWebHook(GitlabAPI api, String projectName) throws IOException {
		List<GitlabProject> projects = api.getProjects();
		for (GitlabProject p : projects) {
			if (p.getName().equals(projectName)) {
				return api.getProjectHooks(p);
			}
		}
		return null;
	}

	public static boolean addWebHookIfNotExisted(GitlabAPI api, String projectName, String url) throws IOException {
		boolean result = Boolean.FALSE;
		GitlabProject target = null;
		List<GitlabProject> projects = api.getProjects();
		for (GitlabProject p : projects) {
			if (p.getName().equals(projectName)) {
				target = p;
			}
		}
		if (target == null) {
			return result;
		}

		boolean isExisted = Boolean.FALSE;
		List<GitlabProjectHook> hooklist = api.getProjectHooks(target);
		for (GitlabProjectHook h : hooklist) {
			if (h.getUrl().equals(url)) {
				isExisted = Boolean.TRUE;
				break;
			}
		}

		if (!isExisted) {
			api.addProjectHook(target, url);
			result = Boolean.TRUE;
		}
		return result;
	}

	public static Map<String, String> getRepositoryUrl(GitlabAPI api, String projectName) throws IOException {
		Map<String, String> urlmap = new HashMap<String, String>();
		GitlabProject target = null;
		List<GitlabProject> projects = api.getProjects();
		for (GitlabProject p : projects) {
			if (p.getName().equals(projectName)) {
				target = p;
			}
		}
		if (target == null) {
			return null;
		}
		urlmap.put("http", target.getHttpUrl());
		urlmap.put("ssh", target.getSshUrl());
		urlmap.put("web", target.getWebUrl());

		return urlmap;
	}
}
