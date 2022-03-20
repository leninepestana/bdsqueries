package com.devsuperior.uri2602;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.devsuperior.uri2602.dto.CustomerNameMinDTO;
import com.devsuperior.uri2602.projections.CustomerNameProjection;
import com.devsuperior.uri2602.repositories.CustomerRepository;

@SpringBootApplication
public class Uri2602Application implements CommandLineRunner {
	
	@Autowired
	private CustomerRepository customerRepository;
	
	public static void main(String[] args) {
		SpringApplication.run(Uri2602Application.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		
		List<CustomerNameProjection> list = customerRepository.search1("rs");
		List<CustomerNameMinDTO> result1 = list.stream().map(x -> new CustomerNameMinDTO(x)).collect(Collectors.toList());
		
		System.out.println("\n*** RESULT SQL PROJECTION");
		
		for (CustomerNameProjection obj : list) {
			System.out.println(obj.getName());
		}
		
		System.out.println("\n\n");
		
		System.out.println("\n*** RESULT ROOT SQL DTO");
		
		for (CustomerNameMinDTO obj : result1) {
			System.out.println(obj.getName());
		}
		
		System.out.println("\n\n");
		
		List<CustomerNameMinDTO> result2 = customerRepository.search2("RS");
		
		System.out.println("\n*** RESULT JPQL");
		
		for (CustomerNameMinDTO obj : result2) {
			System.out.println(obj);
		}
		
	}

}
