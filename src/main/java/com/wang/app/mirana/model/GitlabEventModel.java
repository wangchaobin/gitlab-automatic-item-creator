/*
 * Copyright wangchaobin 2016
 */
package com.wang.app.mirana.model;

import net.sf.json.JSONObject;

/**
 * Created by Wang on 20/1/2016.
 */
public class GitlabEventModel {

	private String eventName;

	private String projectName;

	private String path;

	private String projectId;

	private String owner;

	private String email;

	private String visibility;

	/*
	 * {"event_name":"project_create",
	 * "created_at":"2016-01-18T07:13:41Z","name":"sample","path":"sample",
	 * "path_with_namespace":"paas/sample","project_id":5,
	 * "owner_name":"paas","owner_email":null,"project_visibility":"private"}
	 * 
	 * {"event_name":"project_destroy",
	 * "created_at":"2016-01-20T02:31:26Z","name":"dsafdf","path":"dsafdf",
	 * "path_with_namespace":"root/dsafdf","project_id":50,
	 * "owner_name":"paas","owner_email":"null","project_visibility":"private"}
	 */
	public GitlabEventModel(String event) {
		JSONObject obj = JSONObject.fromObject(event);
		eventName = obj.getString("event_name");
		projectId = obj.getString("project_id");
		visibility = obj.getString("project_visibility");
		projectName = obj.getString("name");
		path = obj.getString("path");
		owner = obj.getString("owner_name");
		email = obj.getString("owner_email");
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getVisibility() {
		return visibility;
	}

	public void setVisibility(String visibility) {
		this.visibility = visibility;
	}

}
