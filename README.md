## BdsQueries


### Chapter 5 - Database Queries

This study is part of Chapter 5 of DevSuperior Spring React Bootcamp tracks, which use Spring Boot and persisting in a memory database using Spring Boot SQL.

## Disclaimer

The following SQL problems to solve were taken out from
https://www.beecrowd.com.br/

### Lesson 05-07 URI 2602 Spring Boot SQL

#### Basic Select

Your company is doing a survey of how many customers are registered in the states, however, lacked to raise the data of the state of the 'Rio Grande do Sul'.

Then, you must show the names of all customers whose state is 'RS'.

Tables

| **id** | **name**                  | **street**               | **city**       | **state** | **credit_limit** |
|--------|---------------------------|--------------------------|----------------|-----------|------------------|
|    1   | Pedro Augusto da Rocha    | Rua Pedro Carlos Hoffman | Porto Alegre   |     RS    |      700,00      |
|    2   | Antonio Carlos Mamel      | Av. Pinheiros            | Belo Horizonte |     MG    |      3500,50     |
|    3   | Luiza Augusta Mhor        | Rua Salto Grande         | Niteroi        |     RJ    |      4000,00     |
|    4   | Jane Ester                | Av 7 de setembro         | Erechim        |     RS    |      800,00      |
|    5   | Marcos Antônio dos Santos | Av Farrapos              | Porto Alegre   |     RS    |      4250,25     |

Output sample

| **name**                  |
|---------------------------|
| Pedro Augusto da Rocha    |
| Jane Ester                |
| Marcos Antônio dos Santos |



Project uri2602 is already started with the main ***Uri2602*** Application class, the ***Customer*** Entity, the ***application.properties*** with dev profile configured and also ***application-dev.properties***.

To work with this project we must have already configured **pgAdmin**, in this case with the uri2602 database.

This lesson consists of making the query below, in the database:

```SQL
SELECT name 
FROM customers 
WHERE state = 'RS'
```

> Important note - Customer class variables must be created based on the SQL below.

> Important note - The ID is not incremental, so we just represent as @Id on the Customer class.

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
  (1,'Pedro Augusto da Rocha','Rua Pedro Carlos Hoffman','Porto Alegre','RS',700.00),
  (2,'Antonio Carlos Mamel','Av. Pinheiros','Belo Horizonte','MG',3500.50),
  (3,'Luiza Augusta Mhor','Rua Salto Grande','Niteroi',	'RJ',4000.00),	
  (4,'Jane Ester','Av 7 de setembro','Erechim','RS',800.00),
  (5,'Marcos Antônio dos Santos','Av Farrapos','Porto Alegre','RS',4250.25);
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

The query I want to make, makes a projection with only the name field.

One way to access the database and select only those fields that I need is to define a projection, in the Spring Boot project.

The projection class, is an interface, only with the signature of the methods.

```java
package com.devsuperior.uri2602.projections;

public interface CustomerNameProjection {

	String getName();
}
```
Step 3 - Update repository class

With the *Projection* implemented, I must update the repository class.

The query line below specify a native SQL query.

```code
@Query(nativeQuery = true)
```
Then I must specify the SQL query in the *value* field.
> Important note: I must not forget that I must leave a space in the last word because of concatenation of the bottom line.


```code
@Query(nativeQuery = true, value = "SELECT name "
                  + "FROM customers "
                  + "WHERE state = :state")
```

The value *:state* on the query from above must be the same word as in the argument search.

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

To test the application I will access the main class, and use the *CommandLineRunner*.

The code placed inside the run method will be executed right at the beginning of the application.

I will do a call from here to *CustomerRepository* to return the database query as we can see with line below passing the value *RS* in the argument of the search.

```java
List<CustomerNameProjection> list = customerRepository.search1("RS");
```

Then I'll make a loop, and for each *CustomerNameProjection obj* inside the *list* I'll print the *obj.getName()*.



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
The *CustomerNameProjection* is an interface that has the *getName()* method, and when I do a search in the *CustomerRepository* returning the interface the *Spring Data JPA* creates a **concret** object with the interface structure.

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

In a practical use, on the web, I will need to use **DTO** and not **Projection**.

The **DTO** will return the result to the **controller** and the **controller** will return to the **API**.

The code below represents the implementation of *CustomerNameMinDTO* class.

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
In the main application class, to represent this, I have to transform the *CustomerNameMinProjection* list into a *CustomerNameMinDTO* list as in the code above.

```java
List<CustomerNameProjection> list = customerRepository.search1("RS");
List<CustomerNameMinDTO> result1 = list.stream()
                .map(x -> new CustomerNameMinDTO(x)).collect(Collectors.toList());
```

With the *CustomerNameMinDTO* list implemented I can now loop on top of the DTO as like in the code below and have the same result.

```java
for (CustomerNameMinDTO obj : result1) {
  System.out.println(obj.getName());
}
```

The final code from the application class is this one below.

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
		List<CustomerNameMinDTO> result1 = list.stream()
                    .map(x -> new CustomerNameMinDTO(x)).collect(Collectors.toList());
		
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

### Lesson 05-08 URI 2602 Spring Boot JPQL

Query the database with JPQL support

>Important note: To prevent problems in searches, related to the values entered by the user, I can use the UPPER or LOWER function.

```sql
@Query(nativeQuery = true, value = "SELECT name "
			+ "FROM customers "
			+ "WHERE UPPER(state) = UPPER(:state)")
List<CustomerNameProjection> search1(String state);
```

In the same way I did before for the native ***SQL*** query, I will define in the *CustomerRepository* class, the ***JPQL*** query.

***JPQL*** query don't need *Projection*, I can directly return *DTO*.

To make the equivalent of the *SQL* query in *JPQL* I have to give the object nickname, here in this case, instead of being *"FROM customers"* I will put *"FROM Customer obj"*, which is exactly the name of the class and its nickname which is *obj* in this case.

If it were to query the entities in the database, that is, just the *Customer*, the query could be done as follows.

```sql
@Query(value = "SELECT obj "
	+ "FROM customers "
	+ "WHERE UPPER(state) = UPPER(:state)")
List<Customer> search2(String state);
```
  But what I want is a Database Projection, that is, just the database name field in this case, a *DTO* that has only the name.

  In this case I have to specify the full path of the *DTO* in the query, creating a new object of the *DTO* passing the constructor from the *CustomerNameMinDTO* class, and then accessing the *name* through the nickname *obj* in this case.

  The *WHERE* clause must also be changed, now I access the *state* with *obj* nickname.

  The ***JPA SQL*** query will look like this:

```sql
@Query(value = "SELECT new com.devsuperior.uri2602.dto.CustomerNameMinDTO(obj.name) "
		+ "FROM Customer obj "
		+ "WHERE UPPER(obj.state) = UPPER(:state)")
List<Customer> search2(String state);
```
***CustomerRepository*** class complete code:

```java
package com.devsuperior.uri2602.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.devsuperior.uri2602.dto.CustomerNameMinDTO;
import com.devsuperior.uri2602.entities.Customer;
import com.devsuperior.uri2602.projections.CustomerNameProjection;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

	@Query(nativeQuery = true, value = "SELECT name "
			+ "FROM customers "
			+ "WHERE UPPER(state) = UPPER(:state)")
	List<CustomerNameProjection> search1(String state);

	@Query(value = "SELECT new com.devsuperior.uri2602.dto.CustomerNameMinDTO(obj.name) "
			+ "FROM Customer obj "
			+ "WHERE UPPER(obj.state) = UPPER(:state)")
	List<CustomerNameMinDTO> search2(String state);
}
```

Main class final code:

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
		
		List<CustomerNameProjection> list = customerRepository.search1("rs");
		List<CustomerNameMinDTO> result1 = list.stream()
              .map(x -> new CustomerNameMinDTO(x)).collect(Collectors.toList());
		
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
```

Result:

```code
Hibernate: 
    SELECT
        name 
    FROM
        customers 
    WHERE
        UPPER(state) = UPPER(?)

*** RESULT SQL PROJECTION
Pedro Augusto da Rocha
Jane Ester
Marcos Antônio dos Santos




*** RESULT SQL DTO
Pedro Augusto da Rocha
Jane Ester
Marcos Antônio dos Santos



Hibernate: 
    select
        customer0_.name as col_0_0_ 
    from
        customers customer0_ 
    where
        upper(customer0_.state)=upper(?)

*** RESULT JPQL
CustomerNameMinDTO [name=Pedro Augusto da Rocha]
CustomerNameMinDTO [name=Jane Ester]
CustomerNameMinDTO [name=Marcos Antônio dos Santos]
```

### Lesson 05-09 URI 2611 Elaborate the query

Problem from https://www.beecrowd.com.br/judge/en/problems/view/2611


### Action Movies

A video store contractor hired her services to catalog her movies. They need you to select the code and the name of the movies whose description of the genre is 'Action'.

movies table:

| **id** | **name**                     | **id_genres** |
|--------|------------------------------|---------------|
|    1   | Batman                       |       3       |
|    2   | The Battle of the Dark River |       3       |
|    3   | White Duck                   |       1       |
|    4   | Breaking Barriers            |       4       |
|    5   | The Two Hours                |       2       |

genres table:

| **id** | **description** |
|--------|-----------------|
|    1   | Animation       |
|    2   | Horror          |
|    3   | Action          |
|    4   | Drama           |
|    5   | Comedy          |


Output sample

| **id** | **name**                     |
|--------|------------------------------|
|    1   | Batman                       |
|    2   | The Battle of the Dark River |


```sql
CREATE TABLE genres (
  id numeric PRIMARY KEY,
  description varchar(50)
);

CREATE TABLE movies (
  id numeric PRIMARY KEY,
  name varchar(50),
  id_genres numeric REFERENCES genres (id)
);

INSERT INTO genres (id, description)
VALUES
  (1,'Animation'),
  (2,'Horror'),
  (3,'Action'),
  (4,'Drama'),
  (5,'Comedy');
  
INSERT INTO movies (id, name, id_genres)
VALUES
  (1,'Batman',3),
  (2,'The Battle of the Dark River',3),
  (3,'White Duck',1),
  (4,'Breaking Barriers',4),
  (5,'The Two Hours',2);
```

I will use the INNER JOIN keyword that is, because it selects records that have matching values in both tables

Query all the movies, and genre tables data with INNER JOIN

```sql
SELECT *
FROM movies
INNER JOIN genres 
ON movies.id_genres = genres.id
```

| **id** | **name**                     | **id_genres** | **id-2** | **description** |
|--------|------------------------------|---------------|----------|-----------------|
|    1   | Batman                       |       3       |     3    | Action          |
|    2   | The Battle of the Dark River |       3       |     3    | Action          |
|    3   | White Duck                   |       1       |     1    | Animation       |
|    4   | Breaking Barriers            |       4       |     4    | Drama           |
|    5   | The Two Hours                |       2       |     2    | Horror          |

Query movies id, name, fields on movies table, where genre are equal to Action

```sql
SELECT movies.id, movies.name
FROM movies
INNER JOIN genres 
ON movies.id_genres = genres.id
WHERE genres.description = 'Action'
```


| **id** | **name**                     |
|--------|------------------------------|
|    1   | Batman                       |
|    2   | The Battle of the Dark River |


### Lesson 05-10 URI 2611 Spring Boot SQL e JPQL

Conceptual model

<p align="left">
<img src="https://user-images.githubusercontent.com/22635013/159188266-4e2a5ef8-cdfa-475b-a009-d02f1aac618d.png">
</p>

Now that I have the query result for URI 2611, I will implement this in Spring Boot.

The project has already started, with the Gender and Movie Entities.

To represent the entities I must first look at the SQL and identify the queries first so that I can build the Genre an Movie classes.

#### Create the Genre and Movies classes

```sql
CREATE TABLE genres (
  id numeric PRIMARY KEY,
  description varchar(50)
);
```
```sql
CREATE TABLE movies (
  id numeric PRIMARY KEY,
  name varchar(50),
  id_genres numeric REFERENCES genres (id)
);
```
#### Genre
As the SQL says, the table I have to create will be called *genres*.

So the class ***Genre*** must have an ***id***, and a ***description*** variables. 

From the diagram above, I can see that the ***Genre*** class has a ***one-to-many*** association that is identified by ***genre***. 

The ***Genre*** class should have a ***List of movies***, as it is a ***one-to-many*** association and will be mapped with ***movies***

```java
package com.devsuperior.uri2611.entities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "genres")
public class Genre {

	@Id
	private Long id;
	private String description;
	
	@OneToMany(mappedBy = "genre")
	private List<Movie> movies = new ArrayList<>();
	
	public Genre() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Movie> getMovies() {
		return movies;
	}
}
```


#### Movie

As the SQL says the ***movies*** table must be created and will have an ***id***,  ***name***, and ***id_genres***. 

The  ***id_genres*** will be the association with two tables. 

The ***Movie*** class has a ***many-to-one*** association, which should be identified with the ***genre*** in the ***id_genres*** field of the table and by the ***Genre genre*** as type.

Movie class

```java
package com.devsuperior.uri2611.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "movies")
public class Movie {

	@Id
	private Long id;
	private String name;
	
	@ManyToOne
	@JoinColumn(name = "id_genres")
	private Genre genre;
	
	public Movie() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Genre getGenre() {
		return genre;
	}

	public void setGenre(Genre genre) {
		this.genre = genre;
	}
}
```
MovieMinProjection class

```java
package com.devsuperior.uri2611.projections;

public interface MovieMinProjection {
	
	Long getId();
	String getName();
}
```
MovieMinDTO class

```java
package com.devsuperior.uri2611.dto;

import java.io.Serializable;

import com.devsuperior.uri2611.projections.MovieMinProjection;

public class MovieMinDTO implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private Long id;
	private String name;
	
	public MovieMinDTO() {
	}
	
	public MovieMinDTO(Long id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public MovieMinDTO(MovieMinProjection projection) {
		id = projection.getId();
		name = projection.getName();
	}
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "MovieNinDTO [" + id + " - " + name + "]";
	}
	
}
```
MovieRepository class

```java
package com.devsuperior.uri2611.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.devsuperior.uri2611.dto.MovieMinDTO;
import com.devsuperior.uri2611.entities.Movie;
import com.devsuperior.uri2611.projections.MovieMinProjection;

public interface MovieRepository extends JpaRepository<Movie, Long> {

	@Query(nativeQuery = true, value = "SELECT movies.id, movies.name "
			+ "FROM movies "
			+ "INNER JOIN genres "
			+ "ON movies.id_genres = genres.id "
			+ "WHERE UPPER(genres.description) = UPPER(:genre)")
	List<MovieMinProjection> search1(String genre);
	
	@Query(value = "SELECT new com.devsuperior.uri2611.dto.MovieMinDTO(obj.id, obj.name) "
			+ "FROM Movie obj "
			+ "WHERE UPPER(obj.genre.description) = UPPER(:genre)")
	List<MovieMinDTO> search2(String genre);
	
}
```
Application runner class

```java
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
		List<MovieMinDTO> result1 = list.stream()
					.map(x -> new MovieMinDTO(x)).collect(Collectors.toList());
		
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
```
Result:

```code
Hibernate: 
    SELECT
        movies.id,
        movies.name 
    FROM
        movies 
    INNER JOIN
        genres 
            ON movies.id_genres = genres.id 
    WHERE
        UPPER(genres.description) = UPPER(?)

*** RESULT NATIVE SQL
1 - Batman
2 - The Battle of the Dark River

*** RESULT JPQL
1 - Batman
2 - The Battle of the Dark River
```




### 05-11 URI 2621 Preparing the query

#### Amounts Between 10 and 20

When it comes to delivering the report on how many products the company has in stock, a part of the report has become corrupted, so the stock keeper asked for help, he wants you to display the following data for him.

Display the name of products whose amount are between 10 and 20 and whose name of the supplier starts with the letter 'P'.

providers

| **id** | **name**           | **street**                    | **city**       | **state** |
|--------|--------------------|-------------------------------|----------------|-----------|
|    1   | Ajax SA            | Rua Presidente Castelo Branco | Porto Alegre   |     RS    |
|    2   | Sansul SA          | Av Brasil                     | Rio de Janeiro |     RJ    |
|    3   | Pr Sheppard Chairs | Rua do Moinho                 | Santa Maria    |     RS    |
|    4   | Elon Electro       | Rua Apolo                     | São Paulo      |     SP    |
|    5   | Mike Electro       | Rua Pedro da Cunha            | Curitiba       |     PR    |

products

| **id** | **name**        | **amount** | **price** | **id_providers** |
|--------|-----------------|------------|-----------|------------------|
|    1   | Blue Chair      | 30         | 300.00    |         5        |
|    2   | Red Chair       | 50         | 2150.00   |         2        |
|    3   | Disney Wardrobe | 400        | 829.50    |         4        |
|    4   | Executive Chair | 17         | 9.90      |         3        |
|    5   | Solar Panel     | 30         | 3000.25   |         4        |

Output sample

| **name**        |
|-----------------|
| Executive Chair |

```sql
CREATE TABLE providers (
  id numeric PRIMARY KEY,
  name varchar(255),
  street varchar(255),
  city varchar(255),
  state char(2)
);

CREATE TABLE products (
  id numeric PRIMARY KEY,
  name varchar (255),
  amount numeric,
  price numeric,
  id_providers numeric REFERENCES providers (id)
);


INSERT INTO providers (id, name, street, city, state)
VALUES
  (1, 'Ajax SA', 'Rua Presidente Castelo Branco', 'Porto Alegre', 'RS'),
  (2, 'Sansul SA', 'Av Brasil', 'Rio de Janeiro', 'RJ'),
  (3, 'Pr Sheppard Chairs', 'Rua do Moinho', 'Santa Maria', 'RS'),
  (4, 'Elon Electro', 'Rua Apolo', 'São Paulo', 'SP'),
  (5, 'Mike Electro', 'Rua Pedro da Cunha', 'Curitiba', 'PR');
  
INSERT INTO products (id, name, amount, price, id_providers)
VALUES
  (1,'Blue Chair', 30, 300.00, 5),
  (2,'Red Chair', 50, 2150.00, 2),
  (3,'Disney Wardrobe', 400, 829.50, 4),
  (4,'Executive Chair', 17, 9.90, 3),
  (5,'Solar Panel', 30, 3000.25, 4);
```


```sql
SELECT products.name
FROM products
INNER JOIN providers ON providers.id = products.id_providers
WHERE products.amount BETWEEN 10 AND 20
AND providers.name LIKE 'P%';
```

### 05-12 URI 2621 Spring Boot SQL and JPQL

Conceptual model

<p align="left">
<img src="https://user-images.githubusercontent.com/22635013/159665703-4f378a24-56d1-4dd7-b2ad-a3a24e3e4bf0.png">
</p>

Product class

```java
package com.devsuperior.uri2621.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "products")
public class Product {

	@Id
	private Long id;
	private String name;
	private Integer amount;
	private Double price;
	
	@ManyToOne
	@JoinColumn(name = "id_providers")
	private Provider provider;
	
	public Product() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getAmount() {
		return amount;
	}

	public void setAmount(Integer amount) {
		this.amount = amount;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public Provider getProvider() {
		return provider;
	}

	public void setProvider(Provider provider) {
		this.provider = provider;
	}
}
```

Provider class

```java
package com.devsuperior.uri2621.entities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "providers")
public class Provider {

	@Id
	private Long id;
	private String name;
	private String street;
	private String city;
	private String state;
	
	@OneToMany(mappedBy = "provider")
	private List<Product> products = new ArrayList<>();
	
	public Provider() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public List<Product> getProducts() {
		return products;
	}
}
```

ProductMinProjection interface

```java
package com.devsuperior.uri2621.projections;

public interface ProductMinProjection {
	
	String getName();
}

```
ProductMinDTO class


```java
package com.devsuperior.uri2621.dto;

import com.devsuperior.uri2621.projections.ProductMinProjection;

public class ProductMinDTO {

		private String name;
		
		public ProductMinDTO() {
		}
		
		public ProductMinDTO(String name) {
			this.name = name;
		}
		
		public ProductMinDTO(ProductMinProjection projection) {
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
			return "[ProductName] " + name;
		}
}
```


ProductRepository class

```java
package com.devsuperior.uri2621.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.devsuperior.uri2621.dto.ProductMinDTO;
import com.devsuperior.uri2621.entities.Product;
import com.devsuperior.uri2621.projections.ProductMinProjection;

public interface ProductRepository extends JpaRepository<Product, Long> {

	@Query(nativeQuery = true, value = "SELECT products.name "
			+ "FROM products "
			+ "INNER JOIN providers ON providers.id = products.id_providers "
			+ "WHERE products.amount BETWEEN :min AND :max "
			+ "AND providers.name LIKE CONCAT( :beginName, '%')")
	List<ProductMinProjection> search1(Integer min, Integer max, String beginName);

	@Query("SELECT new com.devsuperior.uri2621.dto.ProductMinDTO(obj.name) "
			+ "FROM Product obj "
			+ "WHERE obj.amount BETWEEN :min AND :max "
			+ "AND obj.provider.name LIKE CONCAT( :beginName, '%')")
	List<ProductMinDTO> search2(Integer min, Integer max, String beginName);
}
```
Application runner class

```java
package com.devsuperior.uri2621;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.devsuperior.uri2621.dto.ProductMinDTO;
import com.devsuperior.uri2621.projections.ProductMinProjection;
import com.devsuperior.uri2621.repositories.ProductRepository;

@SpringBootApplication
public class Uri2621Application implements CommandLineRunner {

	@Autowired
	private ProductRepository productRepository;
	
	public static void main(String[] args) {
		SpringApplication.run(Uri2621Application.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		
		List<ProductMinProjection> list = productRepository.search1(10, 20, "P");
		List<ProductMinDTO> result1 = list.stream()
				.map(x -> new ProductMinDTO(x)).collect(Collectors.toList());
		
		System.out.println("\n*** RESULT NATIVE SQL");
		
		for (ProductMinDTO obj : result1) {
			System.out.println(obj);
		}
		
		System.out.println("\n\n");
		
		List<ProductMinDTO> result2 = productRepository.search2(10, 20, "P");
		
		System.out.println("\n*** RESULT JPQL");
		for (ProductMinDTO obj : result2) {
			System.out.println(obj);
		}
	}
}
```

Result:

```code
Hibernate: 
    SELECT
        products.name 
    FROM
        products 
    INNER JOIN
        providers 
            ON providers.id = products.id_providers 
    WHERE
        products.amount BETWEEN ? AND ? 
        AND providers.name LIKE CONCAT( ?, '%')

*** RESULT NATIVE SQL
[ProductName] Executive Chair



Hibernate: 
    select
        product0_.name as col_0_0_ 
    from
        products product0_ cross 
    join
        providers provider1_ 
    where
        product0_.id_providers=provider1_.id 
        and (
            product0_.amount between ? and ?
        ) 
        and (
            provider1_.name like (?||'%')
        )

*** RESULT JPQL
[ProductName] Executive Chair
```
### 05-13 - SQL pratice

#### Group 1: projection, restriction (2602, 2603, 2604, 2607, 2608, 2615, 2624)

#### Group 2: JOIN (2605, 2606, 2611, 2613, 2614, 2617, 2618, 2619, 2620, 2621, 2622, 2623, 2742)

#### Group 3: GROUP BY, sub-queries (2609, 2993, 2994, 2995, 2996)

#### Group 4: Expressions and projections (2610, 2625, 2738, 2739, 2741, 2743, 2744, 2745, 2746, 3001)

#### Group 5: Difference, Union (2616, 2737, 2740, 2990)

#### Group 6: Hard (2988, 2989, 2991, 2992, 2997, 2998, 2999)



#### URI2602 Basic Select

Your company is doing a survey of how many customers are registered in the states, however, lacked to raise the data of the state of the 'Rio Grande do Sul'.

Then, you must show the names of all customers whose state is 'RS'.

customers table

| **id** | **name**                  | **street**               | **city**       | **state** | **credit_limit** |
|--------|---------------------------|--------------------------|----------------|-----------|------------------|
|    1   | Pedro Augusto da Rocha    | Rua Pedro Carlos Hoffman | Porto Alegre   |     RS    |      700,00      |
|    2   | Antonio Carlos Mamel      | Av. Pinheiros            | Belo Horizonte |     MG    |      3500,50     |
|    3   | Luiza Augusta Mhor        | Rua Salto Grande         | Niteroi        |     RJ    |      4000,00     |
|    4   | Jane Ester                | Av 7 de setembro         | Erechim        |     RS    |      800,00      |
|    5   | Marcos Antônio dos Santos | Av Farrapos              | Porto Alegre   |     RS    |      4250,25     |

Output sample

| **name**                  |
|---------------------------|
| Pedro Augusto da Rocha    |
| Jane Ester                |
| Marcos Antônio dos Santos |




```sql
--- URI Online Judge SQL
--- Copyright URI Online Judge
--- www.urionlinejudge.com.br
--- Problem 2602

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
  (1, 'Pedro Augusto da Rocha',	'Rua Pedro Carlos Hoffman', 'Porto Alegre', 'RS', 700.00),
  (2, 'Antonio Carlos Mamel', 'Av. Pinheiros', 'Belo Horizonte', 'MG',	3500.50),
  (3, 'Luiza Augusta Mhor', 'Rua Salto Grande', 'Niteroi', 'RJ', 4000.00),	
  (4, 'Jane Ester', 'Av 7 de setembro', 'Erechim', 'RS', 800.00),
  (5, 'Marcos Antônio dos Santos', 'Av Farrapos', 'Porto Alegre', 'RS', 4250.25);

  
  /*  Execute this query to drop the tables */
  -- DROP TABLE customers; --
```

```sql
SELECT name 
FROM customers 
WHERE state = 'RS'
  ```

#### URI2603 Customer Address

The company will make an event celebrating the 20th anniversary of the market, and for that we will make a great celebration in the city of Porto Alegre. We also invite all our customers who are enrolled in this city.

Your job is in having the names and addresses of customers who live in 'Porto Alegre', to deliver the invitations personally.

customers table


| **id** | **name**                  | **street**               | **city**       | **state** | **credit_limit** |
|--------|---------------------------|--------------------------|----------------|-----------|------------------|
|    1   | Pedro Augusto da Rocha    | Rua Pedro Carlos Hoffman | Porto Alegre   |     RS    |      700,00      |
|    2   | Antonio Carlos Mamel      | Av. Pinheiros            | Belo Horizonte |     MG    |      3500,50     |
|    3   | Luiza Augusta Mhor        | Rua Salto Grande         | Niteroi        |     RJ    |      4000,00     |
|    4   | Jane Ester                | Av 7 de setembro         | Erechim        |     RS    |      800,00      |
|    5   | Marcos Antônio dos Santos | Av Farrapos              | Porto Alegre   |     RS    |      4250,25     |


Output sample

| **name**                  | **street**               |
|---------------------------|--------------------------|
| Pedro Augusto da Rocha    | Rua Pedro Carlos Hoffman |
| Marcos Antônio dos Santos | Av Farrapos              |


```sql
--- URI Online Judge SQL
--- Copyright URI Online Judge
--- www.urionlinejudge.com.br
--- Problem 2603

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
  (1, 'Pedro Augusto da Rocha', 'Rua Pedro Carlos Hoffman', 'Porto Alegre', 'RS', 700.00),
  (2, 'Antonio Carlos Mamel', 'Av. Pinheiros', 'Belo Horizonte', 'MG', 3500.50),	
  (3, 'Luiza Augusta Mhor,', 'Rua Salto Grande', 'Niteroi', 'RJ', 4000.00),
  (4, 'Jane Ester', 'Av 7 de setembro', 'Erechim', 'RS', 800.00),
  (5, 'Marcos Antônio dos Santos', 'Av Farrapos', 'Porto Alegre', 'RS', 4250.25);
  
  /*  Execute this query to drop the tables */
  -- DROP TABLE customers; --
  ```

  ```sql
SELECT name, street 
FROM customers
WHERE city = 'Porto Alegre';
```

#### URI2604 - Under 10 or Greater Than 100

The financial sector of the company needs a report that shows the ID and the name of the products whose price is less than 10 or greater than 100.

products table

| **id** | **name**          | **amount** | **price** |
|--------|-------------------|------------|-----------|
|    1   | Two-door wardrobe |     100    |     80    |
|    2   | Dining table      |    1000    |    560    |
|    3   | Towel holder      |    10000   |    5.50   |
|    4   | Computer desk     |     350    |    100    |
|    5   | Chair             |    3000    |   210.64  |
|    6   | Single bed        |     750    |     99    |

Output sample

| **id** | **name**     |
|--------|--------------|
|    2   | Dining table |
|    3   | Towel holder |
|    5   | Chair        |

```sql
CREATE TABLE products (
  id NUMERIC PRIMARY KEY,
  name CHARACTER VARYING (255),
  amount NUMERIC,
  price NUMERIC
);

INSERT INTO products (id, name, amount, price)
VALUES 
  (1, 'Two-door wardrobe', 100, 80),
  (2, 'Dining table', 1000, 560),
  (3, 'Towel holder', 10000, 5.50),
  (4, 'Computer desk', 350, 100),
  (5, 'Chair', 3000, 210.64),
  (6, 'Single bed', 750, 99);
  
  /*  Execute this query to drop the tables */
  -- DROP TABLE products; --
  ```

  ```sql
SELECT id, name FROM products
WHERE price <10
OR price >100;
```


#### URI2607 - Providers' City in Alphabetical Order

Every month the company asks for a report from the cities that providers are registered. So, do a query that returns all the cities of the providers, but in alphabetical order.

OBS: You must not show repeated cities.

providers table

| **id** | **name**         | **street**     | **city**       | **state** |
|--------|------------------|----------------|----------------|-----------|
|    1   | Henrique         | Av Brasil      | Rio de Janeiro |     RJ    |
|    2   | Marcelo Augusto  | Rua Imigrantes | Belo Horizonte |     MG    |
|    3   | Caroline Silva   | Av São Paulo   | Salvador       |     BA    |
|    4   | Guilerme Staff   | Rua Central    | Porto Alegre   |     RS    |
|    5   | Isabela Moraes   | Av Juiz Grande | Curitiba       |     PR    |
|    6   | Francisco Accerr | Av Paulista    | São Paulo      |     SP    |


Output sample


| **city**       |
|----------------|
| Belo Horizonte |
| Curitiba       |
| Porto Alegre   |
| Rio de Janeiro |
| Salvador       |
| São Paulo      |


```sql
--- URI Online Judge SQL
--- Copyright URI Online Judge
--- www.urionlinejudge.com.br
--- Problem 2607

CREATE TABLE providers (
  id numeric PRIMARY KEY,
  name varchar(50),
  street varchar(50),
  city varchar(50),
  state varchar(2)
);

INSERT INTO providers (id, name, street, city, state)
VALUES 
  (1, 'Henrique', 'Av Brasil', 'Rio de Janeiro', 'RJ'),
  (2, 'Marcelo Augusto', 'Rua Imigrantes', 'Belo Horizonte', 'MG'),
  (3, 'Caroline Silva', 'Av São Paulo', 'Salvador', 'BA'),
  (4, 'Guilerme Staff',	'Rua Central',	'Porto Alegre',	'RS'),
  (5, 'Isabela Moraes',	'Av Juiz Grande', 'Curitiba', 'PR'),
  (6, 'Francisco Accerr', 'Av Paulista', 'São Paulo', 'SP');
  
  /*  Execute this query to drop the tables */
  -- DROP TABLE providers; --
    
```

```sql
SELECT providers.city
FROM providers
ORDER BY providers.city ASC
```



#### URI2608 - Higher and Lower Price

The financial sector of our company, wants to know the smaller and higher values of the products, which we sell.

For this you must display only the highest and lowest price of the products table.

products table

| id | name               | amount | price  |
|----|--------------------|--------|--------|
| 1  | Two-doors wardrobe | 100    | 800    |
| 2  | Dining table       | 1000   | 560    |
| 3  | Towel holder       | 10000  | 25.50  |
| 4  | Computer desk      | 350    | 320.50 |
| 5  | Chair              | 3000   | 210.64 |
| 6  | Single bed         | 750    | 460    |


Output sample

| **price** | **price** |
|-----------|-----------|
| 800       | 25.50     |


```sql
CREATE TABLE products (
  id numeric PRIMARY KEY,
  name varchar(50),
  amount numeric,
  price numeric(7,2)
);

INSERT INTO products (id, name, amount, price)
VALUES
  (1, 'Two-doors wardrobe', 100, 800),
  (2, 'Dining table', 1000, 560),
  (3, 'Towel holder', 10000, 25.50),
  (4, 'Computer desk', 350, 320.50),
  (5, 'Chair', 3000, 210.64),
  (6, 'Single bed', 750, 460);
  
  /*  Execute this query to drop the tables */
  -- DROP TABLE products; --
```

```sql
SELECT MAX(price), MIN(price) as price
FROM products
```


#### URI2615 - Expanding the Business

The video store company has the objectives of creating several franchises spread throughout Brazil. For this we want to know in which cities our customers live.

For you to help us select the name of all the cities where the rental company has clients. But please do not repeat the name of the city.

customers table

| **id** | **name**                    | **street**                     | **city**      |
|--------|-----------------------------|--------------------------------|---------------|
|    1   | Giovanna Goncalves Oliveira | Rua Mato Grosso                | Canoas        |
|    2   | Kauã Azevedo Ribeiro        | Travessa Ibiá                  | Uberlândia    |
|    3   | Rebeca Barbosa Santos       | Rua Observatório Meteorológico | Salvador      |
|    4   | Sarah Carvalho Correia      | Rua Antônio Carlos da Silva    | Uberlândia    |
|    5   | João Almeida Lima           | Rua Rio Taiuva                 | Ponta Grossa  |
|    6   | Diogo Melo Dias             | Rua Duzentos e Cinqüenta       | Várzea Grande |

Output sample

| **city**      |
|---------------|
| Uberlândia    |
| Canoas        |
| Ponta Grossa  |
| Várzea Grande |
| Salvador      |

```sql
--- URI Online Judge SQL
--- Copyright URI Online Judge
--- www.urionlinejudge.com.br
--- Problem 2615

CREATE TABLE customers (
  id numeric PRIMARY KEY,
  name varchar(50),
  street varchar(50),
  city varchar(50)
);


INSERT INTO customers (id, name, street, city)
VALUES
  (1, 'Giovanna Goncalves Oliveira', 'Rua Mato Grosso', 'Canoas'),
  (2, 'Kauã Azevedo Ribeiro', 'Travessa Ibiá', 'Uberlândia'),
  (3, 'Rebeca Barbosa Santos', 'Rua Observatório Meteorológico', 'Salvador'),
  (4, 'Sarah Carvalho Correia',	'Rua Antônio Carlos da Silva', 'Uberlândia'),
  (5, 'João Almeida Lima', 'Rua Rio Taiuva', 'Ponta Grossa'),
  (6, 'Diogo Melo Dias', 'Rua Duzentos e Cinqüenta', 'Várzea Grande');
  

/*  Execute this query to drop the tables */
-- DROP TABLE customers; --
```

```sql
SELECT DISTINCT customers.city
FROM customers 
ORDER BY customers.city ASC
```

#### URI2624 - Number of Cities per Customers

The company board asked you for a simple report on how many cities the company has already reached.

To do this you must display the number of distinct cities in the customers table.

customers table

| **id** | **name**                                | **street**                            | **city**      | **state** | **credit_limit** |
|--------|-----------------------------------------|---------------------------------------|---------------|-----------|------------------|
|    1   | Nicolas Diogo Cardoso                   | Acesso Um                             | Porto Alegre  |     RS    |        475       |
|    2   | Cecília Olivia Rodrigues                | Rua Sizuka Usuy                       | Cianorte      |     PR    |       3170       |
|    3   | Augusto Fernando Carlos Eduardo Cardoso | Rua Baldomiro Koerich                 | Palhoça       |     SC    |       1067       |
|    4   | Nicolas Diogo Cardoso                   | Acesso Um                             | Porto Alegre  |     RS    |        475       |
|    5   | Sabrina Heloisa Gabriela Barros         | Rua Engenheiro Tito Marques Fernandes | Porto Alegre  |     RS    |       4312       |
|    6   | Joaquim Diego Lorenzo Araújo            | Rua Vitorino                          | Novo Hamburgo |     RS    |       2314       |


Output sample

| **count** |
|-----------|
| 4         |



```sql
--- URI Online Judge SQL
--- Copyright URI Online Judge
--- www.urionlinejudge.com.br
--- Problem 2624

CREATE TABLE customers (
  id numeric PRIMARY KEY,
  name varchar(255),
  street varchar(255),
  city varchar(255),
  state char(2),
  credit_limit numeric
);


INSERT INTO customers (id, name, street, city, state, credit_limit)
VALUES
(1, 'Nicolas Diogo Cardoso', 'Acesso Um', 'Porto Alegre', 'RS', 475),
(2, 'Cecília Olivia Rodrigues', 'Rua Sizuka Usuy', 'Cianorte', 'PR', 3170),
(3, 'Augusto Fernando Carlos Eduardo Cardoso', 'Rua Baldomiro Koerich', 'Palhoça', 'SC', 1067),
(4, 'Nicolas Diogo Cardoso', 'Acesso Um', 'Porto Alegre', 'RS', 475),
(5, 'Sabrina Heloisa Gabriela Barros', 'Rua Engenheiro Tito Marques Fernandes', 'Porto Alegre', 'RS', 4312)
(6, 'Joaquim Diego Lorenzo Araújo', 'Rua Vitorino', 'Novo Hamburgo', 'RS', 2314);
  
  
/*  Execute this query to drop the tables */
-- DROP TABLE customers; -- 
```

```sql
SELECT COUNT(DISTINCT(customers.city))
FROM customers
```

#### Group 2: JOIN (2605, 2606, 2611, 2613, 2614, 2617, 2618, 2619, 2620, 2621, 2622, 2623, 2742)

#### URI2605 - Executive Representatives

The financial sector needs a report on the providers of the products we sell. The reports include all categories, but for some reason, providers of products whose category is the executive, are not in the report.

Your job is to return the names of the products and providers whose category ID is 6.

products table

| **id** | **name**          | **amount** | **price** | **id_providers** | **id_categories** |
|--------|-------------------|------------|-----------|------------------|-------------------|
|    1   | Two-door wardrobe | 100        | 800       |         6        |         8         |
|    2   | Dining table      | 1000       | 560       |         1        |         9         |
|    3   | Towel holder      | 10000      | 25.50     |         5        |         1         |
|    4   | Computer desk     | 350        | 320.50    |         4        |         6         |
|    5   | Chair             | 3000       | 210.64    |         3        |         6         |
|    6   | Single bed        | 750        | 460       |         1        |         2         |

providers table

| **id** | **name**         | **street**     | **city**       | **state** |
|--------|------------------|----------------|----------------|-----------|
|    1   | Henrique         | Av Brasil      | Rio de Janeiro |     RJ    |
|    2   | Marcelo Augusto  | Rua Imigrantes | Belo Horizonte |     MG    |
|    3   | Caroline Silva   | Av São Paulo   | Salvador       |     BA    |
|    4   | Guilerme Staff   | Rua Central    | Porto Alegre   |     RS    |
|    5   | Isabela Moraes   | Av Juiz Grande | Curitiba       |     PR    |
|    6   | Francisco Accerr | Av Paulista    | São Paulo      |     SP    |

categories table

| **id** | **name**     |
|--------|--------------|
|    1   | old stock    |
|    2   | new stock    |
|    3   | modern       |
|    4   | commercial   |
|    5   | recyclable   |
|    6   | executive    |
|    7   | superior     |
|    8   | wood         |
|    9   | super luxury |
|   10   | vintage      |

Output sample

| **name**      | **name**       |
|---------------|----------------|
| Computer desk | Guilerme Staff |
| Chair         | Caroline Silva |

```sql
--- URI Online Judge SQL
--- Copyright URI Online Judge
--- www.urionlinejudge.com.br
--- Problem 2605

CREATE TABLE categories (
  id numeric PRIMARY KEY,
  name varchar
);

CREATE TABLE providers (
  id numeric PRIMARY KEY,
  name varchar(50),
  street varchar(50),
  city varchar(50),
  state varchar(2)
);

CREATE TABLE products (
  id numeric PRIMARY KEY,
  name varchar(50),
  amount numeric,
  price numeric(7,2),
  id_providers numeric REFERENCES providers (id),
  id_categories numeric REFERENCES categories (id)
);


INSERT INTO categories (id , name)
VALUES 
  (1, 'old stock'),
  (2, 'new stock'),
  (3, 'modern'),
  (4, 'commercial'),
  (5, 'recyclable'),
  (6, 'executive'),
  (7, 'superior'),
  (8, 'wood'),
  (9, 'super luxury'),
  (10, 'vintage');
  
INSERT INTO providers (id, name, street, city, state)
VALUES 
  (1, 'Henrique' ,'Av Brasil', 'Rio de Janeiro', 'RJ'),
  (2, 'Marcelo Augusto', 'Rua Imigrantes', 'Belo Horizonte', 'MG'),
  (3, 'Caroline Silva', 'Av São Paulo', 'Salvador', 'BA'),
  (4, 'Guilerme Staff', 'Rua Central', 'Porto Alegre', 'RS'),
  (5, 'Isabela Moraes', 'Av Juiz Grande', 'Curitiba', 'PR'),
  (6, 'Francisco Accerr', 'Av Paulista', 'São Paulo', 'SP');
  
INSERT INTO products (id, name, amount, price, id_providers, id_categories)
VALUES
  (1, 'Two-door wardrobe', 100, 800, 6, 8),
  (2, 'Dining table', 1000, 560, 1, 9),	
  (3, 'Towel holder', 10000, 25.50, 5, 1),
  (4, 'Computer desk', 350, 320.50, 4, 6),
  (5, 'Chair',	3000, 210.64, 3, 6),
  (6, 'Single bed', 750, 460, 1, 2);
  ```
```sql
SELECT prod.name, prov.name
FROM products prod
INNER JOIN providers prov ON (prod.id_providers = prov.id)
WHERE prod.id_categories = 6
```

#### URI2606 - Categories

When the data were migrated to the database, there was a small misunderstanding on the DBA.

Your boss needs you to select the ID and the name of the products, whose categorie name start with 'super'.

products table

| **id** | **name**           | **amount** | **price** | **id_categories** |
|--------|--------------------|------------|-----------|-------------------|
|    1   | Lampshade          | 100        | 800       |         4         |
|    2   | Table for painting | 1000       | 560       |         9         |
|    3   | Notebook desk      | 10000      | 25.50     |         9         |
|    4   | Computer desk      | 350        | 320.50    |         6         |
|    5   | Chair              | 3000       | 210.64    |         6         |
|    6   | Home alarm         | 750        | 460       |         4         |

categories table

| **id** | **name**     |
|--------|--------------|
|    1   | old stock    |
|    2   | new stock    |
|    3   | modern       |
|    4   | commercial   |
|    5   | recyclable   |
|    6   | executive    |
|    7   | superior     |
|    8   | wood         |
|    9   | super luxury |
|   10   | vintage      |

Output sample

| **id** | **name**           |
|--------|--------------------|
|    2   | Table for painting |
|    3   | Notebook desk      |
|    5   | Chair              |

```sql
--- URI Online Judge SQL
--- Copyright URI Online Judge
--- www.urionlinejudge.com.br
--- Problem 2606

CREATE TABLE categories (
  id numeric PRIMARY KEY,
  name varchar
);

CREATE TABLE products (
  id numeric PRIMARY KEY,
  name varchar(50),
  amount numeric,
  price numeric(7,2),
  id_categories numeric REFERENCES categories (id)
);


INSERT INTO categories (id, name)
VALUES 
  (1, 'old stock'),
  (2, 'new stock'),
  (3, 'modern'),
  (4, 'commercial'),
  (5, 'recyclable'),
  (6, 'executive'),
  (7, 'superior'),
  (8, 'wood'),
  (9, 'super luxury'),
  (10, 'vintage');
  
INSERT INTO products (id , name, amount, price, id_categories)
VALUES
  (1, 'Lampshade', 100, 800, 4),
  (2, 'Table for painting', 1000, 560, 9),
  (3, 'Notebook desk', 10000, 25.50, 9),
  (4, 'Computer desk', 350, 320.50, 6),
  (5, 'Chair', '3000', '210.64', 9),	
  (6, 'Home alarm', 750, 460, 4);
  
/*  Execute this query to drop the tables */
-- DROP TABLE products, categories; --
```

```sql
SELECT prd.id, prd.name
FROM products prd
INNER JOIN categories cat ON(prd.id_categories = cat.id)
WHERE cat.name LIKE '%super%'
```

#### URI2611 - Action Movies

movies table

| **id** | **name**                     | **id_genres** |
|--------|------------------------------|---------------|
|    1   | Batman                       |       3       |
|    2   | The Battle of the Dark River |       3       |
|    3   | White Duck                   |       1       |
|    4   | Breaking Barriers            |       4       |
|    5   | The Two Hours                |       2       |

genres table

| **id** | **description** |
|--------|-----------------|
|    1   | Animation       |
|    2   | Horror          |
|    3   | Action          |
|    4   | Drama           |
|    5   | Comedy          |

Output sample

| **id** | **name**                     |
|--------|------------------------------|
|    1   | Batman                       |
|    2   | The Battle of the Dark River |

```sql
--- URI Online Judge SQL
--- Copyright URI Online Judge
--- www.urionlinejudge.com.br
--- Problem 2611

CREATE TABLE genres (
  id numeric PRIMARY KEY,
  description varchar(50)
);

CREATE TABLE movies (
  id numeric PRIMARY KEY,
  name varchar(50),
  id_genres numeric REFERENCES genres (id)
);

INSERT INTO genres (id, description)
VALUES
  (1,'Animation'),
  (2,'Horror'),
  (3,'Action'),
  (4,'Drama'),
  (5,'Comedy');
  
INSERT INTO movies (id, name, id_genres)
VALUES
  (1,'Batman',3),
  (2,'The Battle of the Dark River',3),
  (3,'White Duck',1),
  (4,'Breaking Barriers',4),
  (5,'The Two Hours',2);

/*  Execute this query to drop the tables */
-- DROP TABLE movies, genres; --
```
```sql
SELECT movies.id, movies.name
FROM movies
INNER JOIN genres 
ON movies.id_genres = genres.id
WHERE genres.description = 'Action'
```

```sql
SELECT mv.id, mv.name
FROM movies mv
INNER JOIN genres gn ON (mv.id_genres = gn.id)
WHERE gn.description LIKE '%Action%'
```
#### URI2613 - Cheap Movies

In the past the studio has made an event where several movies were on sale, we want to know what movies these were.

Your job to help us is to select the ID and name of movies whose price is less than 2.00.

movies table
| **id** | **name**                     | **id_prices** |
|--------|------------------------------|---------------|
|    1   | Batman                       |       3       |
|    2   | The Battle of the Dark River |       3       |
|    3   | White Duck                   |       5       |
|    4   | Breaking Barriers            |       4       |
|    5   | The Two Hours                |       2       |

prices table
| **id** | **categorie** | **value** |
|--------|---------------|-----------|
|    1   | Releases      |    3.50   |
|    2   | Bronze Seal   |    2.00   |
|    3   | Silver Seal   |    2.50   |
|    4   | Gold Seal     |    3.00   |
|    5   | Promotion     |    1.50   |

Output sample

| **id** | **name**   |
|--------|------------|
|    3   | White Duck |

```sql
--- URI Online Judge SQL
--- Copyright URI Online Judge
--- www.urionlinejudge.com.br
--- Problem 2613

CREATE TABLE prices (
  id numeric PRIMARY KEY,
  categorie varchar(50),
  value numeric
);


CREATE TABLE movies (
  id numeric PRIMARY KEY,
  name varchar(50),
  id_prices numeric REFERENCES prices (id)
);

INSERT INTO prices (id , categorie, value)
VALUES
  (1, 'Releases', 3.50),
  (2, 'Bronze Seal', 2.00),
  (3, 'Silver Seal', 2.50),
  (4, 'Gold Seal', 3.00),
  (5, 'Promotion', 1.50);
  
INSERT INTO movies (id, name, id_prices)
VALUES
  (1, 'Batman', 3),
  (2, 'The Battle of the Dark River', 3),
  (3, 'White Duck', 5),
  (4, 'Breaking Barriers', 4),
  (5, 'The Two Hours', 2);
  
  /*  Execute this query to drop the tables */
  -- DROP TABLE movies, prices; --
```
```sql
SELECT mv.id, mv.name
FROM movies mv
INNER JOIN prices pr ON(mv.id_prices = pr.id)
WHERE pr.value < 2.00
```
#### URI2614 - September Rentals

The video store is making its semi-annual report and needs your help. All you have to do is select the name of the clients and the date of rental, from the rentals made in September 2016.

customers table

| **id** | **name**                    | **street**                       | **city**       |
|--------|-----------------------------|----------------------------------|----------------|
|    1   | Giovanna Goncalves Oliveira | Rua Mato Grosso                  | Canoas         |
|    2   | Kauã Azevedo Ribeiro        | Travessa Ibiá                    | Uberlândia     |
|    3   | Rebeca Barbosa Santos       | Rua Observatório  Meteorológico  | Salvador       |
|    4   | Sarah Carvalho Correia      | Rua Antônio  Carlos da Silva     | Apucarana      |
|    5   | João Almeida Lima           | Rua Rio Taiuva                   | Ponta Grossa   |
|    6   | Diogo Melo Dias             | Rua Duzentos e CinqÃ¼enta        | VÃ¡rzea Grande |

rentals table

| **id** | **rentals_date** | **id_customers** |
|--------|------------------|------------------|
|    1   | 09/10/2016       |         3        |
|    2   | 02/09/2016       |         1        |
|    3   | 02/08/2016       |         4        |
|    4   | 02/09/2015       |         2        |
|    5   | 02/03/2016       |         6        |
|    6   | 04/04/2016       |         4        |

Output sample


| **name**                    | **rentals_date** |
|-----------------------------|------------------|
| Giovanna Goncalves Oliveira | 02/09/2016       |

```sql
--- URI Online Judge SQL
--- Copyright URI Online Judge
--- www.urionlinejudge.com.br
--- Problem 2614

CREATE TABLE customers (
  id numeric PRIMARY KEY,
  name varchar(50),
  street varchar(50),
  city varchar(50)
);

CREATE TABLE rentals (
  id numeric PRIMARY KEY,
  rentals_date date,
  id_customers numeric REFERENCES customers (id)
);

INSERT INTO customers (id, name, street, city)
VALUES
  (1, 'Giovanna Goncalves Oliveira', 'Rua Mato Grosso',	'Canoas'),
  (2, 'Kauã Azevedo Ribeiro', 'Travessa Ibiá',	'Uberlândia'),
  (3, 'Rebeca Barbosa Santos', 'Rua Observatório Meteorológico', 'Salvador'),
  (4, 'Sarah Carvalho Correia',	'Rua Antônio Carlos da Silva', 'Apucarana'),
  (5, 'João Almeida Lima', 'Rua Rio Taiuva', 'Ponta Grossa'),
  (6, 'Diogo Melo Dias', 'Rua Duzentos e Cinqüenta', 'Várzea Grande');
  
INSERT INTO rentals (id, rentals_date, id_customers)
VALUES
  (1, '09/10/2016', 3),
  (2, '02/09/2016', 1),
  (3, '02/08/2016', 4),
  (4, '02/09/2015', 2),
  (5, '02/03/2016', 6),
  (6, '04/04/2016', 4);
  
  /*  Execute this query to drop the tables */
  -- DROP TABLE rentals, customers; --
```

```sql
SELECT ct.name, rt.rentals_date
FROM customers ct
INNER JOIN rentals rt ON(ct.id = rt.id_customers)
WHERE rt.rentals_date >= '2016-09-01' 
AND rt.rentals_date <= '2016-09-30'
```


#### URI2617 - Provider Ajax SA

The financial sector has encountered some problems in the delivery of one of our providers, the delivery of the products does not match the invoice.

Your job is to display the name of the products and the name of the provider, for the products supplied by the provider 'Ajax SA'.

providers table

| **id** | **name**     | **street**                | **city**       | **state** |
|--------|--------------|---------------------------|----------------|-----------|
|    1   | Ajax SA      | Presidente Castelo Branco | Porto Alegre   |     RS    |
|    2   | Sansul SA    | Av Brasil                 | Rio de Janeiro |     RJ    |
|    3   | South Chairs | Av Moinho                 | Santa Maria    |     RS    |
|    4   | Elon Electro | Apolo                     | São Paulo      |     SP    |
|    5   | Mike Electro | Pedro da Cunha            | Curitiba       |     PR    |

products table

| **id** | **name**        | **amount** | **price** | **id_providers** |
|--------|-----------------|------------|-----------|------------------|
|    1   | Blue Chair      | 30         | 300       |         5        |
|    2   | Red Chair       | 50         | 2150      |         1        |
|    3   | Disney Wardrobe | 400        | 829.5     |         4        |
|    4   | Blue Toaster    | 20         | 9.9       |         3        |
|    5   | Solar Panel     | 30         | 3000.25   |         4        |

Output sample

| **name**  | **name** |
|-----------|----------|
| Red Chair | Ajax SA  |


```sql
--- URI Online Judge SQL
--- Copyright URI Online Judge
--- www.urionlinejudge.com.br
--- Problem 2617

CREATE TABLE providers (
  id numeric PRIMARY KEY,
  name varchar(255),
  street varchar(255),
  city varchar(255),
  state char(2)
);

CREATE TABLE products (
  id numeric PRIMARY KEY,
  name varchar (255),
  amount numeric,
  price numeric,
  id_providers numeric REFERENCES providers (id)
);


INSERT INTO providers (id, name, street, city, state)
VALUES
  (1, 'Ajax SA', 'Presidente Castelo Branco', 'Porto Alegre', 'RS'),
  (2, 'Sansul SA', 'Av Brasil',	'Rio de Janeiro', 'RJ'),
  (3, 'South Chairs', 'Av Moinho', 'Santa Maria',  'RS'),
  (4, 'Elon Electro', 'Apolo', 'São Paulo', 'SP'),
  (5, 'Mike Electro', 'Pedro da Cunha',	'Curitiba',	'PR');
  
INSERT INTO products (id, name, amount, price, id_providers)
VALUES
  (1, 'Blue Chair', 30, 300.00, 5),
  (2, 'Red Chair',	50,	2150.00, 1),
  (3, 'Disney Wardrobe', 400, 829.50, 4),
  (4, 'Blue Toaster', 20, 9.90, 3),
  (5, 'Solar Panel', 30, 3000.25, 4);
  
  
  /*  Execute this query to drop the tables */
  -- DROP TABLE products, providers; --
```

```sql
SELECT prd.name, prv.name
FROM products prd
INNER JOIN providers prv ON(prd.id_providers = prv.id)
WHERE prv.name LIKE '%Ajax SA%'
```


#### URI2618 - Imported Products

Our company's import sector needs a report on the import of products from our Sansul providers.

Your task is to display the name of the products, the name of the supplier and the name of the category, for the products supplied by the supplier 'Sansul SA' and whose category name is 'Imported'.

products table

| **id** | **name**        | **amount** | **price** | **id_providers** | **id_categories** |
|--------|-----------------|------------|-----------|------------------|-------------------|
|    1   | Blue Chair      | 30         | 300       |         5        |         5         |
|    2   | Red Chair       | 50         | 2150      |         2        |         1         |
|    3   | Disney Wardrobe | 400        | 829.5     |         4        |         1         |
|    4   | Blue Toaster    | 20         | 9.9       |         3        |         1         |
|    5   | TV              | 30         | 3000.25   |         2        |         2         |

providers table

| **id** | **name**     | **street**                    | **city**       | **state** |
|--------|--------------|-------------------------------|----------------|-----------|
|    1   | Ajax SA      | Rua Presidente Castelo Branco | Porto Alegre   |     RS    |
|    2   | Sansul SA    | Av Brasil                     | Rio de Janeiro |     RJ    |
|    3   | South Chairs | Rua do Moinho                 | Santa Maria    |     RS    |
|    4   | Elon Electro | Rua Apolo                     | SÃ£o Paulo     |     SP    |
|    5   | Mike Electro | Rua Pedro da Cunha            | Curitiba       |     PR    |

categories table

| **id** | **name**     |
|--------|--------------|
|    1   | Super Luxury |
|    2   | Imported     |
|    3   | Tech         |
|    4   | Vintage      |
|    5   | Supreme      |

```sql
--- URI Online Judge SQL
--- Copyright URI Online Judge
--- www.urionlinejudge.com.br
--- Problem 2618

CREATE TABLE providers (
  id numeric PRIMARY KEY,
  name varchar(255),
  street varchar(255),
  city varchar(255),
  state char(2)
);

CREATE TABLE categories (
  id numeric PRIMARY KEY,
  name varchar(255)
);

CREATE TABLE products (
  id numeric PRIMARY KEY,
  name varchar (255),
  amount numeric,
  price numeric,
  id_providers numeric REFERENCES providers (id),
  id_categories numeric REFERENCES categories (id)
);

INSERT INTO providers (id, name, street, city, state)
VALUES
  (1, 'Ajax SA', 'Rua Presidente Castelo Branco', 'Porto Alegre', 'RS'),
  (2, 'Sansul SA', 'Av Brasil', 'Rio de Janeiro', 'RJ'),
  (3, 'South Chairs', 'Rua do Moinho', 'Santa Maria', 'RS'),
  (4, 'Elon Electro',	'Rua Apolo', 'São Paulo', 'SP'),
  (5, 'Mike Electro', 'Rua Pedro da Cunha',	'Curitiba',	'PR');

INSERT INTO categories (id, name)
VALUES
  (1, 'Super Luxury'),
  (2, 'Imported'),
  (3, 'Tech'),
  (4, 'Vintage'),
  (5, 'Supreme');
  
INSERT INTO products (id, name, amount, price, id_providers, id_categories)
VALUES
  (1, 'Blue Chair', 30, 300.00,	5,	5),
  (2, 'Red Chair', 50, 2150.00, 2,	1),
  (3, 'Disney Wardrobe', 400, 829.50, 4,	1),
  (4, 'Blue Toaster', 20, 9.90, 3, 1),
  (5, 'TV',	30,	3000.25, 2,	2);
  
  
/*  Execute this query to drop the tables */
-- DROP TABLE products, categories, providers; --
```

```sql
SELECT prd.name, prv.name, cat.name
FROM products prd 
INNER JOIN providers prv ON (prd.id_providers = prv.id)
INNER JOIN categories cat ON (prd.id_categories = cat.id)
WHERE prv.name LIKE '%Sansul SA%' AND cat.name LIKE '%Imported%'
```
OR

```sql
SELECT prd.name, prv.name, cat.name
FROM products prd 
INNER JOIN providers prv ON (prd.id_providers = prv.id)
INNER JOIN categories cat ON (prd.id_categories = cat.id)
WHERE prv.name = 'Sansul SA' AND cat.name = 'Imported'
```



#### URI2619 - Super Luxury

Our company is looking to make a new contract for the supply of new super luxury products, and for this we need some data of our products.

Your job is to display the name of the products, the name of the providers and the price, for the products whose price is greater than 1000 and its category is' Super Luxury.

products table

| **id** | **name**        | **amount** | **price** | **id_providers** | **id_categories** |
|--------|-----------------|------------|-----------|------------------|-------------------|
|    1   | Blue Chair      | 30         | 300       |         5        |         5         |
|    2   | Red Chair       | 50         | 2150      |         2        |         1         |
|    3   | Disney Wardrobe | 400        | 829.5     |         4        |         1         |
|    4   | Blue Toaster    | 20         | 9.9       |         3        |         1         |
|    5   | TV              | 30         | 3000.25   |         2        |         2         |

providers table

| **id** | **name**     | **street**                    | **city**       | **state** |
|--------|--------------|-------------------------------|----------------|-----------|
|    1   | Ajax SA      | Rua Presidente Castelo Branco | Porto Alegre   | RS        |
|    2   | Sansul SA    | Av Brasil                     | Rio de Janeiro | RJ        |
|    3   | South Chairs | Rua do Moinho                 | Santa Maria    | RS        |
|    4   | Elon Electro | Rua Apolo                     | SÃ£o Paulo     | SP        |
|    5   | Mike electro | Rua Pedro da Cunha            | Curitiba       | PR        |

categories table

| **id** | **name**     |
|--------|--------------|
|    1   | Super Luxury |
|    2   | Imported     |
|    3   | Tech         |
|    4   | Vintage      |
|    5   | Supreme      |

Output sample

| **name**  | **name**  | **price** |
|-----------|-----------|-----------|
| Red Chair | Sansul SA | 2150.00   |

```sql
--- URI Online Judge SQL
--- Copyright URI Online Judge
--- www.urionlinejudge.com.br
--- Problem 2619

CREATE TABLE providers (
  id numeric PRIMARY KEY,
  name varchar(255),
  street varchar(255),
  city varchar(255),
  state char(2)
);

CREATE TABLE categories (
  id numeric PRIMARY KEY,
  name varchar(255)
);

CREATE TABLE products (
  id numeric PRIMARY KEY,
  name varchar (255),
  amount numeric,
  price numeric,
  id_providers numeric REFERENCES providers (id),
  id_categories numeric REFERENCES categories (id)
);


INSERT INTO providers (id, name, street, city, state)
VALUES
  (1, 'Ajax SA', 'Rua Presidente Castelo Branco', 'Porto Alegre', 'RS'),
  (2, 'Sansul SA', 'Av Brasil', 'Rio de Janeiro', 'RJ'),
  (3, 'South Chairs', 'Rua do Moinho', 'Santa Maria', 'RS'),
  (4, 'Elon Electro', 'Rua Apolo', 'São Paulo', 'SP'),
  (5, 'Mike electro', 'Rua Pedro da Cunha',	'Curitiba',	'PR');
  
INSERT INTO categories (id, name)
VALUES
  (1, 'Super Luxury'),
  (2, 'Imported'),
  (3, 'Tech'),
  (4, 'Vintage'),
  (5, 'Supreme');
  
INSERT INTO products ( id, name, amount, price, id_providers, id_categories)
VALUES
  (1, 'Blue Chair',	30,	300.00,	5,	5),
  (2, 'Red Chair',	50,	2150.00, 2,	1),
  (3, 'Disney Wardrobe', 400, 829.50, 4, 1),
  (4, 'Blue Toaster', 20, 9.90,	3, 1),
  (5, 'TV',	30,	3000.25, 2,	2);
  
  
/*  Execute this query to drop the tables */
-- DROP TABLE products, categories, providers; --

```
```sql
SELECT prd.name, prv.name, prd.price
FROM products prd
INNER JOIN providers prv ON(prd.id_providers = prv.id)
INNER JOIN categories cat ON(prd.id_categories = cat.id)
WHERE prd.price > 1000
AND cat.name LIKE '%Super Luxury%'
```


#### URI2620 - Orders in First Half

The company's financial audit is asking us for a report for the first half of 2016. Then display the customers name and order number for customers who placed orders in the first half of 2016.

customers table

| **id** | **name**                                | **street**                            | **city**      | **state** | **credit_limit** |
|--------|-----------------------------------------|---------------------------------------|---------------|-----------|------------------|
|    1   | Nicolas Diogo Cardoso                   | Acesso Um                             | Porto Alegre  | RS        |        475       |
|    2   | CecÃ­lia Olivia Rodrigues               | Rua Sizuka Usuy                       | Cianorte      | PR        |       3170       |
|    3   | Augusto Fernando Carlos Eduardo Cardoso | Rua Baldomiro Koerich                 | Palhoça      | SC        |       1067       |
|    4   | Pedro Cardoso                           | Acesso Um                             | Porto Alegre  | RS        |        475       |
|    5   | Sabrina Heloisa Gabriela Barros         | Rua Engenheiro Tito Marques Fernandes | Porto Alegre  | RS        |       4312       |
|    6   | Joaquim Diego Lorenzo Araújo            | Rua Vitorino                          | Novo Hamburgo | RS        |       2314       |

orders table

| **id** | **orders_date** | **id_customers** |
|--------|-----------------|------------------|
|    1   | 13/05/2016      |         3        |
|    2   | 12/01/2016      |         2        |
|    3   | 18/04/2016      |         5        |
|    4   | 07/09/2016      |         4        |
|    5   | 13/02/2016      |         6        |
|    6   | 05/08/2016      |         3        |

Output sample

| **name**                                | **id** |
|-----------------------------------------|--------|
| Augusto Fernando Carlos Eduardo Cardoso |    1   |
| Cecília Olivia Rodrigues                |    2   |
| Sabrina Heloisa Gabriela Barros         |    3   |
| Joaquim Diego Lorenzo Araújo            |    5   |


```sql
--- URI Online Judge SQL
--- Copyright URI Online Judge
--- www.urionlinejudge.com.br
--- Problem 2620

CREATE TABLE customers (
id numeric PRIMARY KEY,
name varchar(255),
street varchar(255),
city varchar(255),
state char(2),
credit_limit numeric
);

CREATE TABLE orders (
id numeric PRIMARY KEY,
orders_date date,
id_customers numeric REFERENCES customers (id)
);


INSERT INTO customers (id, name, street, city, state, credit_limit)
VALUES
(1, 'Nicolas Diogo Cardoso', 'Acesso Um', 'Porto Alegre', 'RS', 475),
(2, 'Cecília Olivia Rodrigues', 'Rua Sizuka Usuy', 'Cianorte', 'PR', 3170),
(3, 'Augusto Fernando Carlos Eduardo Cardoso', 'Rua Baldomiro Koerich', 'Palhoça', 'SC', 1067),
(4, 'Pedro Cardoso', 'Acesso Um', 'Porto Alegre', 'RS', 475),
(5, 'Sabrina Heloisa Gabriela Barros', 'Rua Engenheiro Tito Marques Fernandes', 'Porto Alegre', 'RS', 4312),
(6, 'Joaquim Diego Lorenzo Araújo', 'Rua Vitorino', 'Novo Hamburgo', 'RS', 2314);

INSERT INTO orders (id, orders_date, id_customers)
VALUES
(1, '13/05/2016', 3),
(2, '12/01/2016', 2),
(3, '18/04/2016', 5),
(4, '07/09/2016', 4),
(5, '13/02/2016', 6),
(6, '05/08/2016', 3);


/*  Execute this query to drop the tables */
-- DROP TABLE orders, customers; --
```

```sql
SELECT cust.name, ord.id
FROM customers cust
INNER JOIN orders ord ON (cust.id = ord.id_customers)
WHERE ord.orders_date >= '2016-01-01' AND ord.orders_date <= '2016-06-30'
```


#### URI2621 - Amounts Between 10 and 20

When it comes to delivering the report on how many products the company has in stock, a part of the report has become corrupted, so the stock keeper asked for help, he wants you to display the following data for him.

Display the name of products whose amount are between 10 and 20 and whose name of the supplier starts with the letter 'P'.

providers table

| **id** | **name**           | **street**                    | **city**       | **state** |
|--------|--------------------|-------------------------------|----------------|-----------|
|    1   | Ajax SA            | Rua Presidente Castelo Branco | Porto Alegre   |     RS    |
|    2   | Sansul SA          | Av Brasil                     | Rio de Janeiro |     RJ    |
|    3   | Pr Sheppard Chairs | Rua do Moinho                 | Santa Maria    |     RS    |
|    4   | Elon Electro       | Rua Apolo                     | São Paulo      |     SP    |
|    5   | Mike Electro       | Rua Pedro da Cunha            | Curitiba       |     PR    |

products table

| **id** | **name**        | **amount** | **price** | **id_providers** |
|--------|-----------------|------------|-----------|------------------|
|    1   | Blue Chair      | 30         | 300       |         5        |
|    2   | Red Chair       | 50         | 2150      |         2        |
|    3   | Disney Wardrobe | 400        | 829.5     |         4        |
|    4   | Executive Chair | 17         | 9.9       |         3        |
|    5   | Solar Panel     | 30         | 3000.25   |         4        |

Output sample

| **name**        |
|-----------------|
| Executive Chair |

```sql
--- URI Online Judge SQL
--- Copyright URI Online Judge
--- www.urionlinejudge.com.br
--- Problem 2621

CREATE TABLE providers (
  id numeric PRIMARY KEY,
  name varchar(255),
  street varchar(255),
  city varchar(255),
  state char(2)
);

CREATE TABLE products (
  id numeric PRIMARY KEY,
  name varchar (255),
  amount numeric,
  price numeric,
  id_providers numeric REFERENCES providers (id)
);


INSERT INTO providers (id, name, street, city, state)
VALUES
  (1, 'Ajax SA', 'Rua Presidente Castelo Branco', 'Porto Alegre', 'RS'),
  (2, 'Sansul SA', 'Av Brasil', 'Rio de Janeiro', 'RJ'),
  (3, 'Pr Sheppard Chairs', 'Rua do Moinho', 'Santa Maria', 'RS'),
  (4, 'Elon Electro', 'Rua Apolo', 'São Paulo', 'SP'),
  (5, 'Mike Electro', 'Rua Pedro da Cunha', 'Curitiba', 'PR');
  
INSERT INTO products (id, name, amount, price, id_providers)
VALUES
  (1, 'Blue Chair', 30, 300.00, 5),
  (2, 'Red Chair', 50, 2150.00, 2),
  (3, 'Disney Wardrobe', 400, 829.50, 4),
  (4, 'Executive Chair', 17, 9.90, 3),
  (5, 'Solar Panel', 30, 3000.25, 4);
  
  
/*  Execute this query to drop the tables */
-- DROP TABLE products, providers; --  
```

```sql
SELECT products.name
FROM products
INNER JOIN providers ON providers.id = products.id_providers
WHERE products.amount BETWEEN 10 AND 20
AND providers.name LIKE 'P%';
```
OR

```sql
SELECT prd.name
FROM products prd
INNER JOIN providers prv ON(prd.id_providers = prv.id)
WHERE prv.name LIKE 'P%' AND prd.amount BETWEEN 10 AND 20
```



#### URI2622 - Legal Person

The sales industry wants to make a promotion for all clients that are legal entities. For this you must display the name of the customers that are legal entity.

customers table

| **id** | **name**                                | **street**                            | **city**      | **state** | **credit_limit** |
|--------|-----------------------------------------|---------------------------------------|---------------|-----------|------------------|
|    1   | Nicolas Diogo Cardoso                   | Acesso Um                             | Porto Alegre  | RS        | 475              |
|    2   | CecÃ­lia Olivia Rodrigues               | Rua Sizuka Usuy                       | Cianorte      | PR        | 3170             |
|    3   | Augusto Fernando Carlos Eduardo Cardoso | Rua Baldomiro Koerich                 | Palhoça       | SC        | 1067             |
|    4   | Nicolas Diogo Cardoso                   | Acesso Um                             | Porto Alegre  | RS        | 475              |
|    5   | Sabrina Heloisa Gabriela Barros         | Rua Engenheiro Tito Marques Fernandes | Porto Alegre  | RS        | 4312             |
|    6   | Joaquim Diego Lorenzo Araújo            | Rua Vitorino                          | Novo Hamburgo | RS        | 2314             |

legal_person table

| **id_customers** | **cnpj**   | **contact** |
|------------------|------------|-------------|
|         4        | 8.5884E+13 | 99767-0562  |
|         5        | 4.7774E+13 | 99100-8965  |



```sql
--- URI Online Judge SQL
--- Copyright URI Online Judge
--- www.urionlinejudge.com.br
--- Problem 2622

CREATE TABLE customers (
id numeric PRIMARY KEY,
name varchar(255),
street varchar(255),
city varchar(255),
state char(2),
credit_limit numeric
);

CREATE TABLE legal_person (
id_customers numeric REFERENCES customers (id),
cnpj char (18),
contact varchar(255)
);

INSERT INTO customers (id, name, street, city, state, credit_limit)
VALUES
(1, 'Nicolas Diogo Cardoso', 'Acesso Um', 'Porto Alegre', 'RS', 475),
(2, 'Cecília Olivia Rodrigues', 'Rua Sizuka Usuy', 'Cianorte', 'PR', 3170),
(3, 'Augusto Fernando Carlos Eduardo Cardoso', 'Rua Baldomiro Koerich', 'Palhoça', 'SC', 1067),
(4, 'Nicolas Diogo Cardoso', 'Acesso Um', 'Porto Alegre', 'RS', 475),
(5, 'Sabrina Heloisa Gabriela Barros', 'Rua Engenheiro Tito Marques Fernandes', 'Porto Alegre', 'RS', 4312),
(6, 'Joaquim Diego Lorenzo Araújo', 'Rua Vitorino', 'Novo Hamburgo', 'RS', 2314);

INSERT INTO legal_person (id_customers, cnpj, contact)
VALUES
(4, '85883842000191',	'99767-0562'),
(5, '47773848000117',	'99100-8965');


/*  Execute this query to drop the tables */
-- DROP TABLE legal_person, customers; -- 
```
```sql
SELECT cust.name
FROM customers cust
INNER JOIN legal_person leg ON(cust.id = leg.id_customers)
```


#### URI2623 - Categories with Various Products

The sales industry needs a report to know what products are left in stock.

To help the sales industry, display the product name and category name for products whose amount is greater than 100 and the category ID is 1,2,3,6 or 9. Show the results in ascendent order by category ID.


products table

| **id** | **name**        | **amount** | **price** | **id_categories** |
|--------|-----------------|------------|-----------|-------------------|
|    1   | Blue Chair      | 30         | 300       |         9         |
|    2   | Red Chair       | 200        | 2150      |         2         |
|    3   | Disney Wardrobe | 400        | 829.5     |         4         |
|    4   | Blue Toaster    | 20         | 9.9       |         3         |
|    5   | Solar Panel     | 30         | 3000.25   |         4         |

categories table

| **id** | **name**     |
|--------|--------------|
|    1   | Superior     |
|    2   | Super Luxury |
|    3   | Modern       |
|    4   | Nerd         |
|    5   | Infantile    |
|    6   | Robust       |
|    9   | Wood         |

Output sample

| **name**  | **name**     |
|-----------|--------------|
| Red Chair | Super Luxury |

```sql
--- URI Online Judge SQL
--- Copyright URI Online Judge
--- www.urionlinejudge.com.br
--- Problem 2623

CREATE TABLE categories (
  id numeric PRIMARY KEY,
  name varchar(255)
);

CREATE TABLE products (
  id numeric PRIMARY KEY,
  name varchar (255),
  amount numeric,
  price numeric,
  id_categories numeric REFERENCES categories (id)
);


INSERT INTO categories (id, name)
VALUES
  (1, 'Superior'),
  (2, 'Super Luxury'),
  (3, 'Modern'),
  (4, 'Nerd'),
  (5, 'Infantile'),
  (6, 'Robust'),
  (9, 'Wood');

INSERT INTO products (id, name, amount, price, id_categories)
VALUES
  (1, 'Blue Chair', 30, 300.00, 9),
  (2, 'Red Chair', 200,	2150.00, 2),
  (3, 'Disney Wardrobe', 400, 829.50, 4),
  (4, 'Blue Toaster', 20, 9.90,	3),
  (5, 'Solar Panel', 30, 3000.25, 4);


/*  Execute this query to drop the tables */
-- DROP TABLE products, categories; --
```

```SQL
SELECT  prd.name, cat.name
FROM products prd
INNER JOIN categories cat ON (prd.id_categories = cat.id)
WHERE prd.amount > 100 
AND prd.id_categories IN(1,2,3,4,5,6,9)
ORDER BY cat.id ASC
```



#### URI2742 - Richard's Multiverse

Richard is a famous scientist because of his multiverse theory, where he describes every hypothetical set of parallel universes by means of a database. Thanks to that you now have a job..

As your first task, you must select every Richard from dimensions C875 and C774, together with its existence probability (the famous factor N) with three decimal places of precision.

Remember that the N factor is calculated by multiplying the omega value by 1,618. The data must be sorted by the least omega value.



dimensions table

| **id** | **name** |
|--------|----------|
|    1   |   C774   |
|    2   |   C784   |
|    3   |   C794   |
|    4   |   C824   |
|    5   |   C875   |

life_registry table

| **id** | **name**            | **omega** | **dimensions_id** |
|--------|---------------------|-----------|-------------------|
|    1   | Richard Postman     |    5.6    |         2         |
|    2   | Simple Jelly        |    1.4    |         1         |
|    3   | Richard Gran Master |    2.5    |         1         |
|    4   | Richard Turing      |    6.4    |         4         |
|    5   | Richard Strall      |     1     |         3         |

Output sample

| **name**            | **The N Factor** |
|---------------------|------------------|
| Richard Gran Master | 4.045            |


```sql
SELECT lir.name, ROUND(lir.omega*1.618,3) AS "The N Factor"
FROM life_registry lir 
INNER JOIN dimensions dim ON (lir.dimensions_id = dim.id)
WHERE lir.name LIKE '%Richard%' 
AND dim.name IN ('C875', 'C774')
```

### 05-14 URI 2609 Preparing the query

![Class Diagram URI 2609](https://user-images.githubusercontent.com/22635013/160297439-c76dcc2e-577b-42c8-aaed-1e923278498d.png)

products table

| **id** | **name**           | **amount** | **price** | **id_categories** |
|--------|--------------------|------------|-----------|-------------------|
|    1   | Two-doors wardrobe | 100        | 800       |         1         |
|    2   | Dining table       | 1000       | 560       |         3         |
|    3   | Towel holder       | 10000      | 25.5      |         4         |
|    4   | Computer desk      | 350        | 320.5     |         2         |
|    5   | Chair              | 3000       | 210.64    |         4         |
|    6   | Single bed         | 750        | 460       |         1         |

categories table

| **id** | **name**     |
|--------|--------------|
|    1   | wood         |
|    2   | luxury       |
|    3   | vintage      |
|    4   | modern       |
|    5   | super luxury |

Output sample

| **name** | **sum** |
|----------|---------|
| luxury   | 350     |
| modern   | 13000   |
| vintage  | 1000    |
| wood     | 850     |



```sql
--- URI Online Judge SQL
--- Copyright URI Online Judge
--- www.urionlinejudge.com.br
--- Problem 2609

CREATE TABLE categories (
  id numeric PRIMARY KEY,
  name varchar
);

CREATE TABLE products (
  id numeric PRIMARY KEY,
  name varchar(50),
  amount numeric,
  price numeric(7,2),
  id_categories numeric REFERENCES categories (id)
);

INSERT INTO categories (id, name)
VALUES 
  (1, 'wood'),
  (2, 'luxury'),
  (3, 'vintage'),
  (4, 'modern'),
  (5, 'super luxury');
  
INSERT INTO products (id, name, amount, price, id_categories)
VALUES 
  (1, 'Two-doors wardrobe', 100, 800, 1),
  (2, 'Dining table', 1000, 560, 3),
  (3, 'Towel holder', 10000, 25.50,	4),
  (4, 'Computer desk', 350,	320.50,	2),
  (5, 'Chair', 3000, 210.64, 4),
  (6, 'Single bed',	750, 460, 1);
  
  /*  Execute this query to drop the tables */
  -- DROP TABLE categories,products; --

  ```
```sql
SELECT categories.name, SUM(products.amount)
FROM categories
INNER JOIN products ON (products.id_categories = categories.id)
GROUP BY categories.name
```

***Product*** class implementation

```sql
package com.devsuperior.uri2609.entities;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "products")
public class Product {

	@Id
	private Long id;
	private String name;
	private Integer amount;
	private BigDecimal price;
	
	@ManyToOne
	@JoinColumn(name = "id_categories")
	private Category category;
	
	public Product() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getAmount() {
		return amount;
	}

	public void setAmount(Integer amount) {
		this.amount = amount;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}
}
```
***Category*** class implementation

```sql
package com.devsuperior.uri2609.entities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "categories")
public class Category {

	@Id
	private Long id;
	private String name;
	
	@OneToMany(mappedBy = "category")
	private List<Product> products = new ArrayList<>();
	
	public Category() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Product> getProducts() {
		return products;
	}
}
```
***CategorySumProjection*** class implementation

```sql
package com.devsuperior.uri2609.projections;

public interface CategorySumProjection {

	String getName();
	Long getSum();
}
```
***CategoryRepository*** class implementation

```sql
package com.devsuperior.uri2609.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.devsuperior.uri2609.dto.CategorySumDTO;
import com.devsuperior.uri2609.entities.Category;
import com.devsuperior.uri2609.projections.CategorySumProjection;

public interface CategoryRepository extends JpaRepository<Category, Long> {

	@Query(nativeQuery = true, value = "SELECT categories.name, SUM(products.amount) "
			+ "FROM categories "
			+ "INNER JOIN products ON (products.id_categories = categories.id) "
			+ "GROUP BY categories.name")
	List<CategorySumProjection> search1();
	
	@Query(value = "SELECT new com.devsuperior.uri2609.dto.CategorySumDTO(obj.category.name, SUM(obj.amount)) "
			+ "FROM Product obj "
			+ "GROUP BY obj.category.name")
	List<CategorySumDTO> search2();
	
}
```
***CategorySumDTO*** class implementation

```sql
package com.devsuperior.uri2609.dto;

import com.devsuperior.uri2609.projections.CategorySumProjection;

public class CategorySumDTO {

	private String name;
	private Long sum;
	
	public CategorySumDTO() {
	}

	public CategorySumDTO(String name, Long sum) {
		this.name = name;
		this.sum = sum;
	}

	public CategorySumDTO(CategorySumProjection projection) {
		name = projection.getName();
		sum = projection.getSum();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getSum() {
		return sum;
	}

	public void setSum(Long sum) {
		this.sum = sum;
	}

	@Override
	public String toString() {
		return "CategorySumDTO [name=" + name + ", sum=" + sum + "]";
	}
}
```
***Application*** class implementation

```sql
package com.devsuperior.uri2609;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.devsuperior.uri2609.dto.CategorySumDTO;
import com.devsuperior.uri2609.projections.CategorySumProjection;
import com.devsuperior.uri2609.repositories.CategoryRepository;

@SpringBootApplication
public class Uri2609Application implements CommandLineRunner {

	@Autowired
	private CategoryRepository repository;
	
	public static void main(String[] args) {
		SpringApplication.run(Uri2609Application.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		
		List<CategorySumProjection> list = repository.search1();
		List<CategorySumDTO> result1 = list.stream()
        .map(x -> new CategorySumDTO(x)).collect(Collectors.toList());
		
		System.out.println("\n*** RESULT SQL PROJECTION");
		for (CategorySumDTO obj : result1) {
			System.out.println(obj);
		}		
		System.out.println("\n\n");
	
	
		
		List<CategorySumDTO> result2 = repository.search2();
		
		System.out.println("\n*** RESULT JPQL");
		for (CategorySumDTO obj : result2) {
			System.out.println(obj);
		}
	
		
	}
}
```
Result:

```code
Hibernate: 
    SELECT
        categories.name,
        SUM(products.amount) 
    FROM
        categories 
    INNER JOIN
        products 
            ON (
                products.id_categories = categories.id
            ) 
    GROUP BY
        categories.name

*** RESULT SQL PROJECTION
CategorySumDTO [name=vintage, sum=1000]
CategorySumDTO [name=wood, sum=850]
CategorySumDTO [name=luxury, sum=350]
CategorySumDTO [name=modern, sum=13000]



Hibernate: 
    select
        category1_.name as col_0_0_,
        sum(product0_.amount) as col_1_0_ 
    from
        products product0_ cross 
    join
        categories category1_ 
    where
        product0_.id_categories=category1_.id 
    group by
        category1_.name

*** RESULT JPQL
CategorySumDTO [name=vintage, sum=1000]
CategorySumDTO [name=wood, sum=850]
CategorySumDTO [name=luxury, sum=350]
CategorySumDTO [name=modern, sum=13000]
```

### 05-16 URI 2737 Preparing the query

#### URI2737 - Lawyers

The manager of Mangojata Lawyers requested a report on the current lawyers.

The manager wants you to show him the name of the lawyer with the most clients, the one with the fewest and the client average considering all lawyers.

OBS: Before presenting the average, show a field called Average to make the report more readable. The average must be presented as an integer.

![Class Diagram URI 2737](https://user-images.githubusercontent.com/22635013/160358853-71932f26-362d-4830-ae9b-b81af1f10971.png)

customer lawyer table

| **register** | **name**            | **customers_number** |
|--------------|---------------------|----------------------|
|     1648     | Marty M. Harrison   |           5          |
|     2427     | Jonathan J. Blevins |          15          |
|     3365     | Chelsey D. Sanders  |          20          |
|     4153     | Dorothy W. Ford     |          16          |
|     5525     | Penny J. Cormier    |           6          |

Output sample

| **name**           | **customers_number** |
|--------------------|----------------------|
| Chelsey D. Sanders |          20          |
| Marty M. Harrison  |           5          |
| Average            |          12          |


```sql
--- URI Online Judge SQL
--- Copyright URI Online Judge
--- www.urionlinejudge.com.br
--- Problem 2737

CREATE TABLE lawyers(
  register INTEGER PRIMARY KEY,
  name VARCHAR(255),
  customers_number INTEGER
 );
  
  
 INSERT INTO lawyers(register, name, customers_number)
 VALUES (1648, 'Marty M. Harrison', 5),
	(2427, 'Jonathan J. Blevins', 15),
	(3365, 'Chelsey D. Sanders', 20),
	(4153, 'Dorothy W. Ford', 16),
	(5525, 'Penny J. Cormier', 6);

  
  /*  Execute this query to drop the tables */
  -- DROP TABLE lawyers; --
```
```sql
(SELECT name, customers_number
FROM lawyers
ORDER BY customers_number DESC
LIMIT 1)

UNION ALL

(SELECT name, customers_number
FROM lawyers
ORDER BY customers_number ASC
LIMIT 1)

UNION ALL

(SELECT 'Average', ROUND(AVG(customers_number), 0)
FROM lawyers)
```

### 05-17 URI 2737 Workaround

```sql
(SELECT name, customers_number
FROM lawyers
WHERE customers_number = (
	SELECT MAX(customers_number)
	FROM lawyers)
)

UNION ALL

(SELECT name, customers_number
FROM lawyers
WHERE customers_number = (
	SELECT MIN(customers_number)
	FROM lawyers)
)

UNION ALL

(SELECT 'Average', ROUND(AVG(customers_number), 0)
FROM lawyers)
```

***Lawyer*** class implementation

```java
package com.devsuperior.uri2737.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "lawyers")
public class Lawyer {

	@Id
	private Long register;
	private String name;
	private Integer customersNumber;
	
	public Lawyer() {
	}

	public Long getRegister() {
		return register;
	}

	public void setRegister(Long register) {
		this.register = register;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getCustomersNumber() {
		return customersNumber;
	}

	public void setCustomersNumber(Integer customersNumber) {
		this.customersNumber = customersNumber;
	}
}
```
***LawyerMinProjection*** class implementation

```java
package com.devsuperior.uri2737.projections;

public interface LawyerMinProjection {

	String getName();
	Integer getCustomersNumber();
}
```
***LawyerRepository*** class implementation

```java
package com.devsuperior.uri2737.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.devsuperior.uri2737.entities.Lawyer;
import com.devsuperior.uri2737.projections.LawyerMinProjection;

public interface LawyerRepository extends JpaRepository<Lawyer, Long> {
	
	@Query(nativeQuery = true, value = "(SELECT name, customers_number AS customersNumber "
			+ "FROM lawyers "
			+ "WHERE customers_number = ("
			+ "	SELECT MAX(customers_number) "
			+ "	FROM lawyers) "
			+ ") "
			+ "UNION ALL "
			+ "(SELECT name, customers_number "
			+ "FROM lawyers "
			+ "WHERE customers_number = ( "
			+ "	SELECT MIN(customers_number) "
			+ "	FROM lawyers) "
			+ ") "
			+ "UNION ALL "
			+ "(SELECT 'Average', ROUND(AVG(customers_number), 0) "
			+ "FROM lawyers)")
	List<LawyerMinProjection> search1();
}
```
***Application*** class implementation
```java
package com.devsuperior.uri2737;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.devsuperior.uri2737.dto.LawyerMinDTO;
import com.devsuperior.uri2737.projections.LawyerMinProjection;
import com.devsuperior.uri2737.repositories.LawyerRepository;

@SpringBootApplication
public class Uri2737Application implements CommandLineRunner {

	@Autowired
	private LawyerRepository repository;
	
	public static void main(String[] args) {
		SpringApplication.run(Uri2737Application.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		
		List<LawyerMinProjection> list = repository.search1();
		List<LawyerMinDTO> result1 = list.stream()
				.map(x -> new LawyerMinDTO(x)).collect(Collectors.toList());
		
		System.out.println("\n*** RESULT SQL PROJECTION");
		for (LawyerMinDTO obj : result1) {
			System.out.println(obj);
		}
		
		System.out.println("\n\n");
	}
}
```
Result:

```code
Hibernate: 
    (
        SELECT
            name,
            customers_number AS customersNumber 
        FROM
            lawyers 
        WHERE
            customers_number = (
                SELECT
                    MAX(customers_number)  
                FROM
                    lawyers
            ) 
        ) 
    UNION
    ALL (
        SELECT
            name,
            customers_number 
        FROM
            lawyers 
        WHERE
            customers_number = (
                SELECT
                    MIN(customers_number)  
                FROM
                    lawyers
            ) 
        ) 
    UNION
    ALL (
        SELECT
            'Average',
            ROUND(AVG(customers_number),
            0) 
        FROM
            lawyers
    )

*** RESULT SQL PROJECTION
LawyerMinDTO [name=Chelsey D. Sanders, customersNumber=20]
LawyerMinDTO [name=Marty M. Harrison, customersNumber=5]
LawyerMinDTO [name=Average, customersNumber=12]
```
> The ***UNION*** operator is used to combine the result-set of two or more ***SELECT*** statements.
***JPQL*** does not yet support ***UNION*** operator
It is only possible to do native sql query in ***JPQL*** for ***UNION*** operator

### 05-19 URI 2990 Preparing the query

#### Employees CPF

Show the CPF, employees (empregados) name and department (departamentos) name of the employees that don't work (trabalha) in any project (projetos). The result must be order by CPF.

**Class Diagram URI 2990**

![Class Diagram URI 2990](https://user-images.githubusercontent.com/22635013/160802348-12098318-f1c4-41da-9034-abf8f5b4143b.png)

***Schema***

**Empregados**

| **Column**     | **Type**     |
|----------------|--------------|
| cpf (PK)       | varchar (15) |
| enome          | varchar (60) |
| salario        | float        |
| cpf_supervisor | varchar (15) |
| dnumero        | integer      |

**Empregados**

| **Column**       | **Type**     |
|------------------|--------------|
| dnumero (PK)     | integer      |
| dnome            | varchar (60) |
| cpf_gerente (FK) | varchar (15) |

**Trabalha**

| **Column**   | **Type**     |
|--------------|--------------|
| cpf_emp (FK) | varchar (15) |
| pnumero      | integer      |

**Projetos**

| **Column**   | **Type**     |
|--------------|--------------|
| pnumero (PK) | integer      |
| pnome        | varchar (45) |
| dnumero (FK) | integer      |


***Empregados*** table

| **cpf**      | **enome**         | **salario** | **cpf_supervisor** | **dnumero** |
|--------------|-------------------|-------------|--------------------|-------------|
| 049382234322 | João Silva        | 2350        | 2434332222         | 1010        |
| 586733922290 | Mario Silveira    | 3500        | 2434332222         | 1010        |
| 2434332222   | Aline Barros      | 2350        | (null)             | 1010        |
| 1733332162   | Tulio Vidal       | 8350        | (null)             | 1020        |
| 4244435272   | Juliana Rodrigues | 3310        | (null)             | 1020        |
| 1014332672   | Natalia Marques   | 2900        | (null)             | 1010        |

***Departamentos*** table

| **dnumero** | **dnome** | **cpf_gerente** |
|-------------|-----------|-----------------|
| 1010        | Pesquisa  | 049382234322    |
| 1020        | Ensino    | 2434332222      |


***Trabalha*** table

| **cpf_emp**  | **pnumero** |
|--------------|-------------|
| 49382234322  | 2010        |
| 586733922290 | 2020        |
| 49382234322  | 2020        |

***Projetos*** table

| **pnumero** | **pnome** | **dnumero** |
|-------------|-----------|-------------|
| 2010        | Alpha     | 1010        |
| 2020        | Beta      | 1020        |

Output sample

| **cpf**    | **enome**         | **dnome** |
|------------|-------------------|-----------|
| 1014332672 | Natalia Marques   | Pesquisa  |
| 1733332162 | Tulio Vidal       | Ensino    |
| 2434332222 | Aline Barros      | Pesquisa  |
| 4244435272 | Juliana Rodrigues | Ensino    |



```sql
CREATE TABLE empregados (
  cpf VARCHAR(15) PRIMARY KEY,
  enome CHARACTER VARYING (255),
  salary FLOAT,
  cpf_supervisor VARCHAR(15),
  dnumero NUMERIC
);

CREATE TABLE departamentos (
  dnumero NUMERIC PRIMARY KEY,
  dnome CHARACTER VARYING (60),
  cpf_gerente VARCHAR(15) REFERENCES empregados (cpf)
);

CREATE TABLE trabalha (
  cpf_emp VARCHAR(15) REFERENCES empregados(cpf),
  pnumero numeric
);

CREATE TABLE projetos (
  pnumero NUMERIC PRIMARY KEY,
  pnome VARCHAR(45),
  dnumero NUMERIC REFERENCES departamentos (dnumero)
);

INSERT INTO empregados(cpf, enome, salary, cpf_supervisor, dnumero)
VALUES 
      ('049382234322', 'João Silva', 2350, '2434332222', 1010),
      ('586733922290', 'Mario Silveira', 3500, '2434332222', 1010),
      ('2434332222', 'Aline Barros', 2350, (null), 1010),
      ('1733332162', 'Tulio Vidal', 8350, (null), 1020),
      ('4244435272', 'Juliana Rodrigues', 3310, (null), 1020),
      ('1014332672', 'Natalia Marques', 2900, (null), 1010);

INSERT INTO departamentos(dnumero, dnome, cpf_gerente)
VALUES
      (1010, 'Pesquisa', '049382234322'),
      (1020, 'Ensino', '2434332222');

INSERT INTO trabalha (cpf_emp, pnumero)
VALUES 
  ('049382234322', 2010),
  ('586733922290', 2020),
  ('049382234322', 2020);

INSERT INTO projetos (pnumero, pnome, dnumero)
VALUES 
  (2010, 'Alpha', 1010),
  (2020, 'Beta', 1020);
  ```
In a first phase, to understand the functioning of the question, and of the interception that we do not want, I will first analyze

With this query below, we collect employees who work on a project

```sql
SELECT empregados.cpf, empregados.enome, departamentos.dnome
FROM empregados
INNER JOIN departamentos ON (empregados.dnumero = departamentos.dnumero)
INNER JOIN trabalha ON (empregados.cpf = trabalha.cpf_emp)
INNER JOIN projetos ON (projetos.pnumero = trabalha.pnumero)
```
Output result

| cpf          | enome          | dnome    |
|--------------|----------------|----------|
| 49382234322  | João Silva     | Pesquisa |
| 586733922290 | Mario Silveira | Pesquisa |
| 49382234322  | João Silva     | Pesquisa |


To get the intended result, I have to restrict those employees who don't work

```sql
SELECT empregados.cpf, empregados.enome, departamentos.dnome
FROM empregados
INNER JOIN departamentos ON (empregados.dnumero = departamentos.dnumero)
WHERE empregados.cpf NOT IN (
	SELECT empregados.cpf
	FROM empregados
	INNER JOIN trabalha ON (trabalha.cpf_emp = empregados.cpf)
)
ORDER BY empregados.cpf
```

Output result

| **cpf**    | **enome**         | **dnome** |
|------------|-------------------|-----------|
| 1014332672 | Natalia Marques   | Pesquisa  |
| 1733332162 | Tulio Vidal       | Ensino    |
| 2434332222 | Aline Barros      | Pesquisa  |
| 4244435272 | Juliana Rodrigues | Ensino    |

### 05-20 URI 2990 Workaround

Another way to solve it is to use the INNER JOIN

INNER JOIN collects intercession values plus null values

Another way to solve it is to use the INNER JOIN
INNER JOIN collects intercession values plus null values

For this case, I intend to collect null values, which are employees who are not in any project

The query below gives me all records with all employees (empregados), including repeated values, since I am doing a JOIN with a one-to-many relationship, in relation to the employees (empregados) with the work (trabalha), taking into account that the same employee (empregado) can work (trabalha) on more than one project (projeto)

```sql
SELECT empregados.cpf, empregados.enome, departamentos.dnome
FROM empregados
INNER JOIN departamentos ON (empregados.dnumero = departamentos.dnumero)
LEFT JOIN trabalha ON (empregados.cpf = trabalha.cpf_emp)
ORDER BY cpf
```
| **cpf**      | **enome**         | **dnome** |
|--------------|-------------------|-----------|
| 49382234322  | João Silva        | Pesquisa  |
| 49382234322  | João Silva        | Pesquisa  |
| 1014332672   | Natalia Marques   | Pesquisa  |
| 1733332162   | Tulio Vidal       | Ensino    |
| 2434332222   | Aline Barros      | Pesquisa  |
| 4244435272   | Juliana Rodrigues | Ensino    |
| 586733922290 | Mario Silveira    | Pesquisa  |

When I project a JOIN to many, it also brings up the repeated employees

The query below gives me all records including null values

LEFT JOIN gives me all records including records outside the relationship between employee(s) working (works) on projects, unlike INNER JOIN which brings records only within the relationship

```sql
SELECT empregados.cpf, empregados.enome, departamentos.dnome, trabalha.*
FROM empregados
INNER JOIN departamentos ON (empregados.dnumero = departamentos.dnumero)
LEFT JOIN trabalha ON (empregados.cpf = trabalha.cpf_emp)
ORDER BY cpf
```
Result table

| **cpf**      | **enome**         | **dnome** | **cpf_emp**  | **pnumero** |
|--------------|-------------------|-----------|--------------|-------------|
| 49382234322  | João Silva        | Pesquisa  | 49382234322  | 2020        |
| 49382234322  | João Silva        | Pesquisa  | 49382234322  | 2010        |
| 1014332672   | Natalia Marques   | Pesquisa  | NULL         | NULL        |
| 1733332162   | Tulio Vidal       | Ensino    | NULL         | NULL        |
| 2434332222   | Aline Barros      | Pesquisa  | NULL         | NULL        |
| 4244435272   | Juliana Rodrigues | Ensino    | NULL         | NULL        |
| 586733922290 | Mario Silveira    | Pesquisa  | 586733922290 | 2020        |

I can restrict the query in order to get only null records from work (trabalha)

```sql
SELECT empregados.cpf, empregados.enome, departamentos.dnome, trabalha.*
FROM empregados
INNER JOIN departamentos ON (empregados.dnumero = departamentos.dnumero)
LEFT JOIN trabalha ON (empregados.cpf = trabalha.cpf_emp)
WHERE trabalha.cpf_emp IS null
ORDER BY cpf
```


Result table

| **cpf**    | **enome**         | **dnome** | **cpf_emp** | **pnumero** |
|------------|-------------------|-----------|-------------|-------------|
| 1014332672 | Natalia Marques   | Pesquisa  | NULL        | NULL        |
| 1733332162 | Tulio Vidal       | Ensino    | NULL        | NULL        |
| 2434332222 | Aline Barros      | Pesquisa  | NULL        | NULL        |
| 4244435272 | Juliana Rodrigues | Ensino    | NULL        | NULL        |


To get the perfect result, I'll remove the trabala.* from the query

```sql
SELECT empregados.cpf, empregados.enome, departamentos.dnome
FROM empregados
INNER JOIN departamentos ON (empregados.dnumero = departamentos.dnumero)
LEFT JOIN trabalha ON (empregados.cpf = trabalha.cpf_emp)
WHERE trabalha.cpf_emp IS null
ORDER BY cpf
```

Result table


| **cpf**    | **enome**         | **dnome** |
|------------|-------------------|-----------|
| 1014332672 | Natalia Marques   | Pesquisa  |
| 1733332162 | Tulio Vidal       | Ensino    |
| 2434332222 | Aline Barros      | Pesquisa  |
| 4244435272 | Juliana Rodrigues | Ensino    |





