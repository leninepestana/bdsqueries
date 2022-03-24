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
  (1,	'Giovanna Goncalves Oliveira',	'Rua Mato Grosso',	'Canoas'),
  (2, 'Kauã Azevedo Ribeiro',	'Travessa Ibiá',	'Uberlândia'),
  (3,	'Rebeca Barbosa Santos',	'Rua Observatório Meteorológico',	'Salvador'),
  (4,	'Sarah Carvalho Correia',	'Rua Antônio Carlos da Silva',	'Uberlândia'),
  (5,	'João Almeida Lima',	'Rua Rio Taiuva',	'Ponta Grossa'),
  (6,	'Diogo Melo Dias',	'Rua Duzentos e Cinqüenta',	'Várzea Grande');
  

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
