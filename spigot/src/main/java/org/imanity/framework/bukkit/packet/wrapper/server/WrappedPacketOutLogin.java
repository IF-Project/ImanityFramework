package org.imanity.framework.bukkit.packet.wrapper.server;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.WorldType;
import org.imanity.framework.bukkit.packet.PacketDirection;
import org.imanity.framework.bukkit.packet.type.PacketType;
import org.imanity.framework.bukkit.packet.type.PacketTypeClasses;
import org.imanity.framework.bukkit.packet.wrapper.SendableWrapper;
import org.imanity.framework.bukkit.packet.wrapper.WrappedPacket;
import org.imanity.framework.bukkit.packet.wrapper.annotation.AutowiredWrappedPacket;
import org.imanity.framework.bukkit.reflection.resolver.MethodResolver;
import org.imanity.framework.bukkit.reflection.wrapper.ConstructorWrapper;
import org.imanity.framework.bukkit.reflection.wrapper.MethodWrapper;

@AutowiredWrappedPacket(value = PacketType.Server.LOGIN, direction = PacketDirection.WRITE)
@Getter
@Setter
public class WrappedPacketOutLogin extends WrappedPacket implements SendableWrapper {

    private static Class<?> PACKET_CLASS, WORLD_TYPE_CLASS;
    private static Class<? extends Enum> ENUM_GAMEMODE_CLASS, ENUM_DIFFICULTY_CLASS;
    private static MethodWrapper NMS_WORLD_TYPE_GET_BY_NAME, NMS_WORLD_TYPE_NAME;
    private static ConstructorWrapper<?> PACKET_CONSTRUCTOR;

    private int playerId;
    private boolean hardcore;
    private GameMode gameMode;
    private int dimension;
    private Difficulty difficulty;
    private int maxPlayers;
    private WorldType worldType;
    private boolean reducedDebugInfo;

    public WrappedPacketOutLogin(Object packet) {
        super(packet);
    }

    public WrappedPacketOutLogin() {
        super();
    }

    public WrappedPacketOutLogin(int playerId, boolean hardcore, GameMode gameMode, int dimension, Difficulty difficulty, int maxPlayers, WorldType worldType, boolean reducedDebugInfo) {
        super();
        this.playerId = playerId;
        this.hardcore = hardcore;
        this.gameMode = gameMode;
        this.dimension = dimension;
        this.difficulty = difficulty;
        this.maxPlayers = maxPlayers;
        this.worldType = worldType;
        this.reducedDebugInfo = reducedDebugInfo;
    }

    public static void init() {

        PACKET_CLASS = PacketTypeClasses.Server.LOGIN;

        try {
            try {
                ENUM_GAMEMODE_CLASS = NMS_CLASS_RESOLVER.resolve("EnumGamemode");
            } catch (Throwable throwable) {
                ENUM_GAMEMODE_CLASS = NMS_CLASS_RESOLVER.resolve("WorldSettings$EnumGamemode");
            }

            ENUM_DIFFICULTY_CLASS = NMS_CLASS_RESOLVER.resolve("EnumDifficulty");
            WORLD_TYPE_CLASS = NMS_CLASS_RESOLVER.resolve("WorldType");

            MethodResolver methodResolver = new MethodResolver(WORLD_TYPE_CLASS);
            NMS_WORLD_TYPE_GET_BY_NAME = methodResolver.resolve(WORLD_TYPE_CLASS, 0, String.class);
            NMS_WORLD_TYPE_NAME = methodResolver.resolve(String.class, 0);

            PACKET_CONSTRUCTOR = new ConstructorWrapper<>(PACKET_CLASS.getDeclaredConstructor(
                    int.class,
                    ENUM_GAMEMODE_CLASS,
                    boolean.class,
                    int.class,
                    ENUM_DIFFICULTY_CLASS,
                    int.class,
                    WORLD_TYPE_CLASS,
                    boolean.class
            ));
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }

    }

    @Override
    protected void setup() {
        this.playerId = readInt(0);

        this.hardcore = readBoolean(0);

        Enum gameModeEnum = readObject(0, ENUM_GAMEMODE_CLASS);
        if (gameModeEnum != null && !gameModeEnum.name().equals("NOT_SET")) {
            this.gameMode = GameMode.valueOf(gameModeEnum.name());
        }

        this.dimension = readInt(1);

        Enum difficultyEnum = readObject(0, ENUM_DIFFICULTY_CLASS);
        if (difficultyEnum != null) {
            this.difficulty = Difficulty.valueOf(difficultyEnum.name());
        }

        Object worldType = readObject(0, WORLD_TYPE_CLASS);
        if (worldType != null) {
            this.worldType = WorldType.getByName((String) NMS_WORLD_TYPE_NAME.invoke(null, worldType));
        }

        this.maxPlayers = readInt(2);

        this.reducedDebugInfo = readBoolean(0);
    }

    @Override
    public Object asNMSPacket() {
        return PACKET_CONSTRUCTOR.newInstance(
                this.playerId,
                this.hardcore,
                Enum.valueOf(ENUM_GAMEMODE_CLASS, this.gameMode.name()),
                this.dimension,
                Enum.valueOf(ENUM_DIFFICULTY_CLASS, this.difficulty.name()),
                this.maxPlayers,
                NMS_WORLD_TYPE_GET_BY_NAME.invoke(null, this.worldType.getName()),
                this.reducedDebugInfo
        );
    }
}
