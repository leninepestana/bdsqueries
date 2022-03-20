## BdsQueries


### Chapter 5 - Database Queries

This study is part of Chapter 5 of DevSuperior Spring React Bootcamp tracks, which use Spring Boot and persisting in a memory database using Spring Boot SQL.

## Disclaimer
https://www.beecrowd.com.br/
### Lesson 05-07 URI 2602 Spring Boot SQL

Project uri2602 is already started with the main ***Uri2602*** Application class, the ***Customer*** Entity, the ***application.properties*** with dev profile configured and also ***application-dev.properties***.

To work with this project we must have already configured **pgAdmin**, in this case with the uri2602 database.

This lesson consists of making the query below, in the database:

```SQL
SELECT name 
FROM customers 
WHERE state = 'RS'
```

> Important note - Customer class variables must be created based on the SQL below.

> Important note - The ID is not incremental, so we just represent as @Id on the Customer class 

```SQL
CREATE TABLE customers (
  id NUMERIC PRIMARY KEY,
  name CHARACTER VARYING (255),
  street CHARACTER VARYING (255),
  city CHARACTER VARYING (255),
  state CHAR (2),
  credit_limit NUMERIC
);

INSERT INTO customers (id, name, street, city, state, credit_limit)
VALUES 
  (1,	'Pedro Augusto da Rocha',	'Rua Pedro Carlos Hoffman',	'Porto Alegre',	'RS',	700.00),
  (2,	'Antonio Carlos Mamel',	'Av. Pinheiros', 'Belo Horizonte',	'MG',	3500.50),
  (3,	'Luiza Augusta Mhor',	'Rua Salto Grande',	'Niteroi',	'RJ',	4000.00),	
  (4,	'Jane Ester',	'Av 7 de setembro',	'Erechim',	'RS',	800.00),
  (5, 'Marcos Antônio dos Santos',	'Av Farrapos',	'Porto Alegre',	'RS',	4250.25);
```
Customers class

```java
@Entity
@Table(name = "customers")
public class Customers {

    @Id
    private Long id;
    private String name;
    private String street;
    private String city;
    private String state;
    private Double creditLimit;
}
```

Step 1 - Create the Repository

```java
package com.devsuperior.uri2602.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.devsuperior.uri2602.entities.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

}
```
Step 2 - Create a projection

The query we want to make, makes a projection with only the name field.

One way to access the database and select only those fields that we need is to define a projection, in the Spring Boot project.

The projection class, is an interface, only with the signature of the methods.

```java
package com.devsuperior.uri2602.projections;

public interface CustomerNameProjection {

	String getName();
}
```
Step 3 - Update repository class

With the Projection implemented, we must update the repository class

The query line below specify a native SQL querie

```code
@Query(nativeQuery = true)
```
Then we must specify the SQL querie in the *value* field
> Important note: We must not forget that we must leave a space in the last word because of concatenation of the bottom line


```code
@Query(nativeQuery = true, value = "SELECT name "
                  + "FROM customers "
                  + "WHERE state = :state")
```

The value *:state* on the querie from above must be the same word as in the argument search

```java
package com.devsuperior.uri2602.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.devsuperior.uri2602.entities.Customer;
import com.devsuperior.uri2602.projections.CustomerNameProjection;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

	@Query(nativeQuery = true, value = "SELECT name "
			+ "FROM customers "
			+ "WHERE state = :state")
	List<CustomerNameProjection> search1(String state);
}
```

To test the application I will test it in the main class, with the CommandLineRunner

The code placed inside the run method will be executed right at the beginning of the application

I will do a call from here to CustomerRepository to return the database querie as we can see with line below passing the value *RS* in the argument of the search

```code
List<CustomerNameProjection> list = customerRepository.search1("RS");
```

Then I'll make a loop, and for each *CustomerNameProjection obj* inside the *list* I'll print the *obj.getName()*



```java
package com.devsuperior.uri2602;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
		
		List<CustomerNameProjection> list = customerRepository.search1("RS");
		
		for (CustomerNameProjection obj : list) {
			System.out.println(obj.getName());
		}
		
	}

}
```
The *CustomerNameProjection* is an interface that has the *getName()* method, and when I do a search in the *CustomerRepository* returning the interface the *Spring Data JPA* creates a **concret** object with the interface structure

Result:

```code
Hibernate: 
    SELECT
        name 
    FROM
        customers 
    WHERE
        state = ?
Pedro Augusto da Rocha
Jane Ester
Marcos Antônio dos Santos
```

In a practical use, on the web, I will need to use **DTO** and not **Projection**

The **DTO** will return the result to the **controller** and the **controller** will return to the **API**

The code below represents the implementation of CustomerNameMinDTO class

```java
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
```
In the main application class, to represent this, I have to transform the CustomerNameMinProjection list into a CustomerNameMinDTO list as in the code above

```java
List<CustomerNameProjection> list = customerRepository.search1("RS");
List<CustomerNameMinDTO> result1 = list.stream().map(x -> new CustomerNameMinDTO(x)).collect(Collectors.toList());
```

With the *CustomerNameMinDTO* list implemented I can now loop on top of the DTO as like in the code below and have the same result

```java
for (CustomerNameMinDTO obj : result1) {
  System.out.println(obj.getName());
}
```

The final code from the application class is this one below

```java
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
		
		List<CustomerNameProjection> list = customerRepository.search1("RS");
		List<CustomerNameMinDTO> result1 = list.stream().map(x -> new CustomerNameMinDTO(x)).collect(Collectors.toList());
		
		for (CustomerNameProjection obj : list) {
			System.out.println(obj.getName());
		}
		
		System.out.println("------------------------------------");
		
		for (CustomerNameMinDTO obj : result1) {
			System.out.println(obj.getName());
		}
		
	}

}

```

