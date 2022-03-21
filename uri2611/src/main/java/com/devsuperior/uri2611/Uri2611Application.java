package com.devsuperior.uri2611;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.devsuperior.uri2611.dto.MovieMinDTO;
import com.devsuperior.uri2611.projections.MovieMinProjection;
import com.devsuperior.uri2611.repositories.MovieRepository;

@SpringBootApplication
public class Uri2611Application implements CommandLineRunner {
	
	@Autowired
	private MovieRepository movieRepository;
	
	public static void main(String[] args) {
		SpringApplication.run(Uri2611Application.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		
		List<MovieMinProjection> list = movieRepository.search1("Action");
		List<MovieMinDTO> result1 = list.stream().map(x -> new MovieMinDTO(x)).collect(Collectors.toList());
		
		System.out.println("\n*** RESULT NATIVE SQL");
		
		for (MovieMinProjection obj : list) {
			System.out.println(obj.getId() + " - " + obj.getName());
		}
		
		System.out.println("\n*** RESULT JPQL");
		for (MovieMinDTO obj : result1) {
			System.out.println(obj.getId() + " - " + obj.getName());
		}
	}
}
