package com.y2kstore.backend.dto;

import com.y2kstore.backend.entity.Address;

public class AddressDTO {
    private Integer id;
    private Integer userId;
    private String userFullName;
    private String province;
    private String district;
    private String ward;
    private String detail;
    private Boolean isDefault;

    public AddressDTO() {}

    public AddressDTO(Address address) {
        this.id = address.getId();
        this.province = address.getProvince();
        this.district = address.getDistrict();
        this.ward = address.getWard();
        this.detail = address.getDetail();
        this.isDefault = address.getIsDefault();
        if (address.getUser() != null) {
            this.userId = address.getUser().getId();
            this.userFullName = address.getUser().getFullName();
        }
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getUserFullName() { return userFullName; }
    public void setUserFullName(String userFullName) { this.userFullName = userFullName; }
    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getWard() { return ward; }
    public void setWard(String ward) { this.ward = ward; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }
}
