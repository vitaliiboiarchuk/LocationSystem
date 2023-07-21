CREATE TABLE users
(
    id       INT AUTO_INCREMENT,
    name     VARCHAR(255),
    password VARCHAR(255),
    username VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE TABLE locations
(
    id      INT AUTO_INCREMENT,
    name    VARCHAR(255),
    address VARCHAR(255),
    user_id INT,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE accesses
(
    id          INT AUTO_INCREMENT,
    title       VARCHAR(255),
    location_id INT,
    user_id     INT,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE,
    FOREIGN KEY (location_id) REFERENCES locations (id)
        ON DELETE CASCADE
);

CREATE TABLE history (
                         id INT AUTO_INCREMENT PRIMARY KEY,
                         object_type VARCHAR(255),
                         action_type VARCHAR(50),
                         details TEXT
);
