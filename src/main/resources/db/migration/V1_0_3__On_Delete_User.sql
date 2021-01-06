CREATE OR REPLACE FUNCTION update_email_username_on_delete() RETURNS TRIGGER AS
$$
BEGIN
    IF OLD.is_deleted = FALSE AND NEW.is_deleted = TRUE THEN
        UPDATE users
        SET email    = concat(email, ' - ', now()),
            username = concat(username, ' - ', now())
        WHERE id = NEW.id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql VOLATILE
                    COST 100;


CREATE TRIGGER on_user_delete
    AFTER UPDATE
    ON users
    FOR EACH ROW
EXECUTE PROCEDURE update_email_username_on_delete();


