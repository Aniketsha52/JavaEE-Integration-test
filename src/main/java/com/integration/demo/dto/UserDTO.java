package com.integration.demo.dto;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "USERS")
@Data
public class UserDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "USER_ID", length = 50)
	private String userId;

	@Column(name = "USER_NAME", length = 255, nullable = false)
	private String userName;

	@Column(name = "PASSWORD", length = 255, nullable = false)
	private String password;

	@Column(name = "CONTACT_NAME", length = 255, nullable = false)
	private String contactName;
	
}
