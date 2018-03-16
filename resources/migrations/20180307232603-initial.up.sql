
CREATE TABLE project (
  id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  description VARCHAR(300),
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  completed BOOL NOT NULL
);
--;;
CREATE TABLE iteration (
  id INT PRIMARY KEY AUTO_INCREMENT,
  num INT NOT NULL,
  description VARCHAR(300),
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  project_id INT,
  FOREIGN KEY (project_id) REFERENCES project(id)
);
--;;
CREATE TABLE stakeholder (
  id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  description VARCHAR(300)
);
--;;
CREATE TABLE specification (
  id INT PRIMARY KEY AUTO_INCREMENT,
  reason VARCHAR(100) NOT NULL,
  spec VARCHAR(300) NOT NULL,
  stakeholder_id INT,
  iteration_id INT,
  FOREIGN KEY (stakeholder_id) REFERENCES stakeholder(id),
  FOREIGN KEY (iteration_id) REFERENCES iteration(id)
);

