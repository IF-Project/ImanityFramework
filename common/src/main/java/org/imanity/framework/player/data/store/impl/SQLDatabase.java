package org.imanity.framework.player.data.store.impl;

import org.imanity.framework.util.builder.SQLColumn;
import org.imanity.framework.util.builder.SQLColumnOption;
import org.imanity.framework.util.builder.SQLColumnType;
import org.imanity.framework.util.builder.SQLTable;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.player.data.PlayerData;
import org.imanity.framework.player.data.type.Data;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;

public class SQLDatabase extends AbstractDatabase {

    private String tableName;
    private SQLTable table;

    @Override
    public void init(String name) {
        this.tableName = ImanityCommon.SQL.getConfig().TABLE_PREFIX + name;
        this.table = new SQLTable(this.tableName)
            .addColumn(new SQLColumn(SQLColumnType.TEXT, "uuid", SQLColumnOption.NOTNULL, SQLColumnOption.PRIMARY_KEY))
            .addColumn(new SQLColumn(SQLColumnType.TINYTEXT, "name"));

        this.getDataTypes().forEach((key, value) -> {
            Data<?> emptyData = value.newData();

            this.table.addColumn(new SQLColumn(emptyData.columnType(), key));
        });

        this.table.executeUpdate(this.table.createQuery()).dataSource(ImanityCommon.SQL.getDataSource()).run();
    }

    @Override
    public void load(PlayerData playerData) {
        this.table.executeSelect("WHERE uuid = ?")
                .dataSource(ImanityCommon.SQL.getDataSource())
                .statement(s -> {
                    s.setString(1, playerData.getUuid().toString());
                }).result(result -> {
            if (result.isBeforeFirst()) {
                result.next();
                this.getDataTypes().forEach((key, value) -> {
                    try {
                        Field field = playerData.getClass().getDeclaredField(key);
                        field.setAccessible(true);
                        field.set(key, result.getObject(key));
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });
            }
            return null;
        }).run();
    }

    @Override
    public void save(PlayerData playerData) {
        List<Data<?>> dataList = playerData.toDataList();

        StringBuilder stringBuilder = new StringBuilder()
                .append("INSERT INTO ")
                .append(tableName)
                .append(" VALUES (");

        Iterator<Data<?>> iterator = dataList.iterator();
        while (iterator.hasNext()) {
            stringBuilder.append(iterator.next().toStringData(true));

            if (iterator.hasNext()) {
                stringBuilder.append(", ");
            }
        }

        stringBuilder.append(") ON DUPLICATE KEY UPDATE ");
        iterator = dataList.iterator();
        while (iterator.hasNext()) {
            Data<?> data = iterator.next();
            stringBuilder.append(data.name())
                         .append(" = ")
                         .append(data.toStringData(true));

            if (iterator.hasNext()) {
                stringBuilder.append(", ");
            }
        }

        stringBuilder.append(";");

        this.table.executeUpdate(stringBuilder.toString())
                .dataSource(ImanityCommon.SQL.getDataSource())
                .run();
    }
}