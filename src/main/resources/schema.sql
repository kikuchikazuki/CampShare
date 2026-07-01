CREATE TABLE users (
   id BIGSERIAL PRIMARY KEY,
   username VARCHAR(50) NOT NULL,
   email VARCHAR(255) NOT NULL UNIQUE,
   password VARCHAR(255) NOT NULL,
   role VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER',
   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE gears (
   id BIGSERIAL PRIMARY KEY,
   name VARCHAR(100) NOT NULL,
   category VARCHAR(50) NOT NULL,
   description TEXT,
   stock INT NOT NULL DEFAULT 0,
   image_url VARCHAR(255),
   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE rentals (
     id BIGSERIAL PRIMARY KEY,

     user_id BIGINT NOT NULL,
     gear_id BIGINT NOT NULL,

     rental_date DATE NOT NULL DEFAULT CURRENT_DATE,
     due_date DATE NOT NULL,
     return_date DATE,

     status VARCHAR(20) NOT NULL DEFAULT 'RENTING',

     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

     CONSTRAINT fk_rental_user
         FOREIGN KEY (user_id)
             REFERENCES users(id)
             ON DELETE CASCADE,

     CONSTRAINT fk_rental_gear
         FOREIGN KEY (gear_id)
             REFERENCES gears(id)
             ON DELETE CASCADE
);