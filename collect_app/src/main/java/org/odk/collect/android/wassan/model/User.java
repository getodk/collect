package org.odk.collect.android.wassan.model;

public class User {
    private String userId,username,password,token,projectId,projectName,projectColor,projectIcon, email,fullname ,phone,position,district,block,gp,roleId, photo;
    private String projects;

    public User(){

    }
    public User(String userId, String username,String password, String token,String projectId,String projectName, String projectColor,String projectIcon,String email, String fullname, String phone, String position, String district, String block, String gp, String roleId, String photo,String projects) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.token = token;
        this.projectId = projectId;
        this.projectName = projectName;
        this.projectColor = projectColor;
        this.projectIcon = projectIcon;
        this.email = email;
        this.fullname = fullname;
        this.phone = phone;
        this.position = position;
        this.district = district;
        this.block = block;
        this.gp=gp;
        this.roleId = roleId;
        this.photo = photo;
        this.projects = projects;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectColor() {
        return projectColor;
    }

    public void setProjectColor(String projectColor) {
        this.projectColor = projectColor;
    }

    public String getProjectIcon() {
        return projectIcon;
    }

    public void setProjectIcon(String projectIcon) {
        this.projectIcon = projectIcon;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getBlock() {
        return block;
    }

    public void setBlock(String block) {
        this.block = block;
    }

    public String getGp() {
        return gp;
    }

    public void setGp(String gp) {
        this.gp = gp;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getProjects() {
        return projects;
    }

    public void setProjects(String projects) {
        this.projects = projects;
    }
}
