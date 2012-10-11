package fi.evident.dalesbred.example;

import fi.evident.dalesbred.Database;
import fi.evident.dalesbred.TransactionCallback;
import fi.evident.dalesbred.TransactionContext;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;

public class Main {

    @NotNull
    public final Database db = Database.forUrlAndCredentials("jdbc:postgresql://db/dalesbred-test", "evident", "Evident11");

    public void exampleTransaction() {
        db.update("drop table if exists department");
        db.update("create table department (id serial primary key, name varchar(64) not null)");
        db.update("insert into department (name) values ('foo')");
        db.update("insert into department (name) values ('bar')");

        int count = db.findUniqueInt("select count(*) from department");
        List<Department> departments = db.findAll(Department.class, "select id, name from department");

        System.out.println("department count: " + count);
        System.out.println("departments: " + departments);
    }

    public void run() {
        db.withTransaction(new TransactionCallback<Object>() {
            @Override
            public Object execute(TransactionContext tx) throws SQLException {
                exampleTransaction();
                tx.setRollbackOnly();
                return null;
            }
        });
    }

    public static void main(String[] args) {
        new Main().run();
    }
}
