/*
 * Copyright wangchaobin 2016
 */
package com.wang.app.mirana.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.wang.app.mirana.model.JenkinsItemModel;

import hudson.maven.MavenModuleSet;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.TopLevelItem;
import jenkins.model.Jenkins;

/**
 * Created by Wang on 20/1/2016.
 */
public class JenkinsUtil {

	private static Jenkins jenkins = Jenkins.getInstance();
	public static String TEMPLATE_MAVEN_CONFIG = "mavenTemplate";

	public static boolean isItemExisting(String projectName) {
		boolean result = Boolean.FALSE;
		List<Item> items = Jenkins.getInstance().getAllItems();
		for (Item i : items) {
			if (i.getName().equals(projectName) && i.getDisplayName().equals(projectName)) {
				return Boolean.TRUE;
			}
		}
		return result;
	}

	public static Item createJenkinsItem(String projectName, JenkinsItemModel item) throws IOException {
		InputStream mavenTemplateConfigXml = item.getConfig();
		Item newItem = jenkins.createProjectFromXML(projectName, mavenTemplateConfigXml);
		return newItem;
	}

	public static Item copyJenkinsItem(String tempProjItem, String newItemName) throws IOException {
		TopLevelItem mavenTemplateItem = jenkins.getItem(tempProjItem);
		ItemGroup<? extends Item> parent = mavenTemplateItem.getParent();
		Item newItem = new MavenModuleSet(parent, newItemName);
		// newItem.onCopiedFrom(mavenTemplateItem);
		newItem = jenkins.copy(mavenTemplateItem, newItemName);
		return newItem;
	}

}
