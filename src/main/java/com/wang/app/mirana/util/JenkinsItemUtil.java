/*
 * Copyright wangchaobin 2016
 */
package com.wang.app.mirana.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import com.wang.app.mirana.model.JenkinsItemModel;

import hudson.XmlFile;
import hudson.model.Item;
import hudson.model.Items;

/**
 * Created by Wang on 20/1/2016.
 */
public class JenkinsItemUtil {

	public static JenkinsItemModel getInstance(String path) throws JDOMException, IOException {

		JenkinsItemModel item = null;
		if (new File(path).exists()) {
			item = new JenkinsItemModel(path);
		}
		if (null == item || item.getDocument() == null) {
			item = new JenkinsItemModel();
		}
		if (null == item || item.getDocument() == null) {
			String root = System.getProperty("JENKINS_HOME");
			if (!root.endsWith("/")) {
				root += "/";
			}
			if (new File(root + "job/mavenTemplate/config.xml").exists()) {
				root += "job/mavenTemplate/config.xml";
			} else {
				root += "config.xml";
			}
			item = new JenkinsItemModel(root);
		}
		return item;
	}

	public static File getConfigAfterModifyProperty(Item templateItem, String repositoryUrl)
			throws JDOMException, IOException {
		XmlFile template = Items.getConfigFile(templateItem);
		InputStream stream = new FileInputStream(template.getFile());
		Document document = new SAXBuilder().build(stream);
		Element element = document.getRootElement().getChild("scm").getChild("userRemoteConfigs")
				.getChild("hudson.plugins.git.UserRemoteConfig");
		element.getChild("url").setText(repositoryUrl);

		// Export the modified xml to temp.
		XMLOutputter xmlopt = new XMLOutputter();
		String tempPath = templateItem.getRootDir() + "/temp.xml";
		FileWriter fw = new FileWriter(tempPath);
		Format fm = Format.getPrettyFormat();
		xmlopt.setFormat(fm);
		xmlopt.output(document, fw);
		fw.close();
		stream.close();
		return new File(tempPath);
	}

}
