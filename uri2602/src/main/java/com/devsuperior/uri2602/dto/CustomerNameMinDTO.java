package com.devsuperior.uri2602.dto;

import java.io.Serializable;

import com.devsuperior.uri2602.projections.CustomerNameProjection;

public class CustomerNameMinDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	private String name;
	
	public CustomerNameMinDTO() {
	}
	
	public CustomerNameMinDTO(String name) {
		this.name = name;
	}
	
	public CustomerNameMinDTO(CustomerNameProjection projection) {
		name = projection.getName();
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return "CustomerNameMinDTO [name=" + name + "]";
	}
		
}
