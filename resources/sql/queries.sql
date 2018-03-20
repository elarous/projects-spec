-- :name create-project! :! :n
-- :doc create a new project record
INSERT INTO project
       VALUES (NULL,:name,:description,:start_date,:end_date,:completed,:img_src)

-- :name delete-project! :! :n
-- :doc deletes a project record given the id
DELETE FROM project
       WHERE id = :id

-- :name update-project! :! :n
-- :doc update an existing project record for the given id
UPDATE project
       SET name = :name,
           description = :description,
           start_date = :start_date,
           end_date = :end_date,
           completed = :completed,
           img_src = :img_src
       WHERE id = :id

-- :name get-project :? :1
-- :doc retrieves a project record given the id
SELECT * FROM project WHERE id = :id

-- :name get-all-projects :? :*
-- :doc retrieves all the projects
SELECT * FROM project

-- :name get-last-project :? :1
-- :doc get the last inserted project
SELECT * FROM project ORDER BY id DESC LIMIT 1;

-- :name update-project-img :! :n
-- :doc update the image of a project
UPDATE project
       SET img_src = :img_src
           WHERE id = :id

-- :name create-iteration! :! :n
-- :doc create an iteration record
INSERT INTO iteration
       VALUES (NULL,:num,:description,:start_date,:end_date,:project_id)

-- :name delete-iteration! :! :n
-- :doc delete the iteration record given id
DELETE FROM iteration
       WHERE id = :id

-- :name update-iteration! :! :n
-- :doc update an existing iteration record given id
UPDATE iteration
       SET num = :num,
           description = :description,
           start_date = :start_date,
           end_date = :end_date,
           project_id = :project_id
       WHERE id = :id

-- :name get-iteration :? :1
-- :doc retrieves an iteration record give id
SELECT * FROM iteration WHERE id = :id

-- :name get-all-iterations :? :*
-- :doc retrieves all iterations
SELECT * FROM iteration

-- :name get-max-iter-num :? :1
-- :doc get the max iteration number
SELECT MAX(num) AS max_num FROM iteration WHERE project_id = :project_id

-- :name get-last-iteration :? :1
-- :doc get the last inserted iteration
SELECT * FROM iteration WHERE project_id = :project_id ORDER BY id DESC LIMIT 1;

-- :name delete-iters-by-project! :! :n
-- :doc delete all iterations for the given project
DELETE FROM iteration WHERE project_id = :project_id

-- :name create-stakeholder! :! :n
-- :doc create a stakeholder record
INSERT INTO stakeholder
       VALUES (NULL,:name,:description,:project_id,:fg_color,:bg_color)


-- :name delete-stakeholder! :! :n
-- :doc delete a stakeholder record given id
DELETE FROM stakeholder WHERE id = :id

-- :name update-stakeholder! :! :n
-- :doc update a stakeholder record given id
UPDATE stakeholder
       SET name = :name,
           description = :description,
           project_id = :project_id,
           fg_color = :fg_color,
           bg_color = :bg_color
       WHERE id = :id

-- :name get-stakeholder :? :1
-- :doc retrieves a stakeholder given id
SELECT * FROM stakeholder WHERE id = :id

-- :name get-all-stakeholders :? :*
-- :doc retrieves all stakeholders
SELECT * FROM stakeholder

-- :name get-last-stakeholder :? :1
-- :doc retrieves the last inserted stakeholder
SELECT * FROM stakeholder WHERE project_id = :project_id ORDER BY id DESC LIMIT 1

-- :name create-spec! :! :n
-- :doc create a specification record
INSERT INTO specification
       VALUES (NULL,:reason,:spec,:stakeholder_id,:iteration_id)

-- :name delete-spec! :! :n
-- :doc delete a specification record given id
DELETE FROM specification WHERE id = :id

-- :name delete-specs-by-iter! :! :n
-- :doc delete all specs within an iteration
DELETE FROM specification WHERE iteration_id = :iteration_id

-- :name update-spec! :! :n
-- :doc update a specification record given id
UPDATE specification
       SET reason = :reason,
           spec = :spec,
           stakeholder_id = :stakeholder_id,
           iteration_id = :iteration_id
       WHERE id = :id

-- :name get-spec :? :1
-- :doc retrieves a specification given id
SELECT * FROM specification WHERE id = :id

-- :name get-all-specs :? :*
-- :doc retrieves all specifications
SELECT * FROM specification


-- :name get-project-iterations :? :*
-- :doc get iterations of a project
SELECT * FROM iteration
       WHERE project_id = :project_id

-- :name get-iteration-specs :? :*
-- :doc get specifications of an iteration
SELECT * FROM specification
       WHERE iteration_id = :iteration_id

