# Secured Order Management

A proof-of-concept REST API built with Java 25 and Spring Boot. It stores users
and products in PostgreSQL (configured for Supabase), uses BCrypt password
hashing, and protects product operations with HTTP Basic authentication and
role/ownership authorization.

## Requirements

- Java 25
- PostgreSQL/Supabase database
- Network access to the configured database

The Maven wrapper is included, so a separate Maven installation is not
required.

## Configuration and startup

Production database settings are read from environment variables:

| Variable | Default |
| --- | --- |
| `DATABASE_URL` | `jdbc:postgresql://db.lswlbxmbdzwlkrwaovum.supabase.co:5432/postgres?sslmode=require` |
| `DATABASE_USERNAME` | `postgres` |
| `DATABASE_PASSWORD` | Required; no default |

In PowerShell, provide the database password and start the application:

```powershell
$env:DATABASE_PASSWORD = "<your-Supabase-database-password>"
.\mvnw.cmd spring-boot:run
```

The production configuration uses `spring.jpa.hibernate.ddl-auto=none`; it
does not create or alter tables. Keep database credentials out of source
control. Use HTTPS when exposing the API because HTTP Basic credentials are
only Base64-encoded, not encrypted.

## Supabase schema setup

The application expects `public.users` to have `id`, `username`, `name`,
`password`, `created_at`, and `role` columns. It expects `public.product` to
have `id`, `name`, `price`, and the quoted `"userId"` column.

The original schema provided for this project does not include `users.username`,
does not generate `product.id`, and defines the product foreign key on
`product.id` instead of `"userId"`. Before using the API, update the schema to
match the application. Review existing rows first; resolve missing/duplicate
usernames, null values, and invalid product owners before adding constraints.
The migration notes in [HELP.md](HELP.md) include example SQL for these
adjustments.

## API

All request and response bodies use JSON. Usernames are case-insensitive for
authentication, are stored lowercase at registration, and may contain
letters, digits, periods, underscores, and hyphens (maximum 64 characters).
Passwords are stored as BCrypt hashes and are never included in user responses.

### Register a user

`POST /users/register` is public. Registration accepts the selected role for
this proof of concept:

```json
{
  "username": "shop-owner",
  "name": "Shop Owner",
  "password": "change-this-password",
  "role": "SELLER"
}
```

`role` must be `CUSTOMER`, `SELLER`, or `ADMIN`. **This is deliberately
insecure for a POC:** anyone can register with the `ADMIN` role. Do not deploy
this registration behavior to production without restricting role assignment.

### Products

All product routes require HTTP Basic authentication. For writes, include the
product name and a non-negative price:

```json
{
  "name": "Wireless mouse",
  "price": 9.0
}
```

| Method | Path | Access |
| --- | --- | --- |
| `GET` | `/products` | Any authenticated user |
| `GET` | `/products/{id}` | Any authenticated user |
| `POST` | `/products` | `SELLER` or `ADMIN` |
| `PUT` | `/products/{id}` | Admin, or the seller who owns the product |
| `DELETE` | `/products/{id}` | Admin, or the seller who owns the product |

The owner is set from the authenticated account on create; clients cannot
choose `"userId"`. Sellers cannot modify or delete products owned by other
sellers. Admins can modify or delete any product. Service-level authorization
uses Spring method security (`@PreAuthorize`) plus a database-backed ownership
check.

### Users

| Method | Path | Access |
| --- | --- | --- |
| `POST` | `/users/register` | Public |
| `GET` | `/users` | `ADMIN` |

Authentication uses the `username` field and the registered password. For
example, use a REST client that supports HTTP Basic and supply the username and
password for each protected request.

## Validation and tests

Registration and product request bodies are validated. Invalid fields receive
a `400 Bad Request`; unauthenticated requests are rejected, and authenticated
users lacking the required role or ownership receive `403 Forbidden`.

Tests use an in-memory H2 database and do not require Supabase credentials:

```powershell
.\mvnw.cmd test
```

To build the application:

```powershell
.\mvnw.cmd package
```
