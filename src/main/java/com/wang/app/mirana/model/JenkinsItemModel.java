/*
 * Copyright wangchaobin 2016
 */
package com.wang.app.mirana.model;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import hudson.model.Item;

/*
 * JenkinsItemModel stands for a build project in Jenkins.
 * Created by Wang on 20/1/2016.
 */
public class JenkinsItemModel {

	private InputStream config;
	private Document document;
	private String template = "template/config.xml";

	public InputStream getConfig() {
		return config;
	}

	public void setConfig(InputStream config) {
		this.config = config;
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public JenkinsItemModel() throws JDOMException, IOException {
		config = new FileInputStream(template);
		document = new SAXBuilder().build(config);
	}

	public JenkinsItemModel(String path) throws JDOMException, IOException {
		template = path;
		config = new FileInputStream(template);
		document = new SAXBuilder().build(config);
	}

	public JenkinsItemModel(Item item) throws JDOMException, IOException {
		String path = item.getRootDir().getAbsolutePath();
		if (!path.endsWith("/")) {
			path += "/";
		}
		path += "config.xml";
		template = path;
		config = new FileInputStream(template);
		document = new SAXBuilder().build(config);
	}

	@SuppressWarnings("unchecked")
	public List<Element> getRootElement(String path) throws Exception {
		return document.getRootElement().getChildren();
	}

	public String getRemoteUrl() throws Exception {
		Element element = document.getRootElement().getChild("scm").getChild("userRemoteConfigs")
				.getChild("hudson.plugins.git.UserRemoteConfig");
		return element.getChildText("url");
	}

	public Document setRemoteUrl(String url) {
		try {
			Element element = document.getRootElement().getChild("scm").getChild("userRemoteConfigs")
					.getChild("hudson.plugins.git.UserRemoteConfig");
			element.getChild("url").setText(url);
			return document;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public InputStream setRemoteUrlD(String url) {
		try {
			Element element = document.getRootElement().getChild("scm").getChild("userRemoteConfigs")
					.getChild("hudson.plugins.git.UserRemoteConfig");
			element.getChild("url").setText(url);
			return config;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void flash(Document doc) {
		try {
			XMLOutputter xmlopt = new XMLOutputter();
			FileWriter writer = new FileWriter(template);
			Format fm = Format.getPrettyFormat();
			// fm.setEncoding("GB2312");
			xmlopt.setFormat(fm);
			xmlopt.output(doc, writer);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void flash(Document doc, String path) {
		try {
			XMLOutputter xmlopt = new XMLOutputter();
			FileWriter writer = new FileWriter(path);
			Format fm = Format.getPrettyFormat();
			xmlopt.setFormat(fm);

			xmlopt.output(doc, writer);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
