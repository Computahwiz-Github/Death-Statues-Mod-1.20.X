package net.isaiah.deathstatues.entity.deathstatue;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.isaiah.deathstatues.DeathStatues;
import net.isaiah.deathstatues.block.statue.DeathStatueBaseBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DeathStatueEntity extends LivingEntity {
    private final DeathStatueInventory inventory = new DeathStatueInventory(this);
    public static final TrackedData<Byte> PLAYER_MODEL_PARTS = DataTracker.registerData(DeathStatueEntity.class, TrackedDataHandlerRegistry.BYTE);
    protected Vec3d lastVelocity = Vec3d.ZERO;
    @Nullable
    private PlayerListEntry playerListEntry;

    public DeathStatueEntity(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public UUID getUuid() {
        return super.getUuid();
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(PLAYER_MODEL_PARTS, (byte)0);
    }

    public static DefaultAttributeContainer.Builder createStatueAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 1.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.0f)
                .add(EntityAttributes.GENERIC_ATTACK_SPEED)
                .add(EntityAttributes.GENERIC_LUCK)
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20);
    }

    @Environment(value= EnvType.CLIENT)
    @Nullable
    protected PlayerListEntry getPlayerListEntry() {
        if (this.playerListEntry == null) {
            //Hopefully this fixes skin texture assigning on online servers
            if (this.getWorld().isClient()) {
                this.playerListEntry = Objects.requireNonNull(MinecraftClient.getInstance().getNetworkHandler()).getPlayerListEntry(getPlayerUUIDFromStatueName(this.getName().getString()));
            }
        }
        return this.playerListEntry;
    }

    @Environment(value=EnvType.CLIENT)
    public UUID getPlayerUUIDFromStatueName(String entityName) {
        //Gets characters from between two square brackets, "[ ]"
        Pattern pattern = Pattern.compile("\\[(.*?)]");
        Matcher matcher = pattern.matcher(entityName);
        String uuidString = "uuidString";

        // Find the first matching pattern (if any)
        if (matcher.find()) {
            try {
                String playerName = matcher.group(1);

                if (!playerName.startsWith("Player")) {
                    uuidString = Objects.requireNonNull(getPlayerUUID(playerName))
                            .replaceAll(
                                    "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                                    "$1-$2-$3-$4-$5"
                            );
                    return UUID.fromString(uuidString);
                }
                else {
                    return Uuids.getOfflinePlayerUuid(playerName);
                }
            } catch (IllegalArgumentException e) {
                // UUID parsing failed
                DeathStatues.LOGGER.info("UUID STRING: " + uuidString);
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String getPlayerUUID(String playerName) {
        try {
            // Create the URL for the Mojang API request
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + playerName);

            // Open a connection to the URL
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Check if the request was successful
            if (connection.getResponseCode() == 200) {
                // Parse the response
                InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                JsonObject response = JsonParser.parseReader(reader).getAsJsonObject();

                // Extract the UUID from the response and remove "" (quotation characters)
                return response.get("id").toString().replaceAll("\"", "");
            } else {
                // Handle error response
                System.err.println("Error: Unable to retrieve UUID. HTTP response code: " + connection.getResponseCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Environment(value=EnvType.CLIENT)
    public Identifier getSkinTexture() {
        PlayerListEntry playerListEntry = this.getPlayerListEntry();
        if (playerListEntry == null) {
            //assert MinecraftClient.getInstance().player != null;
            //return DefaultSkinHelper.getTexture(MinecraftClient.getInstance().player.getUuid()); //Old getTexture
            return DefaultSkinHelper.getTexture(getPlayerUUIDFromStatueName(this.getName().getString()));
        } else {
            return playerListEntry.getSkinTexture();
        }
    }

    @Override
    public Iterable<ItemStack> getArmorItems() {
        return this.inventory.armor;
    }

    @Override
    public Iterable<ItemStack> getHandItems() {
        return Lists.newArrayList(this.inventory.getMainHandStack(), this.getOffHandStack());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        NbtList nbtList = nbt.getList("Inventory", NbtElement.COMPOUND_TYPE);
        this.inventory.readNbt(nbtList);
        this.inventory.selectedSlot = nbt.getInt("SelectedItemSlot");
        }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        NbtHelper.putDataVersion(nbt);
        nbt.put("Inventory", this.inventory.writeNbt(new NbtList()));
        nbt.putInt("SelectedItemSlot", this.inventory.selectedSlot);
    }

    @Override
    public ItemStack getEquippedStack(EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAINHAND) {
            return this.inventory.getMainHandStack();
        }
        if (slot == EquipmentSlot.OFFHAND) {
            return this.inventory.offHand.get(0);
        }
        if (slot.getType() == EquipmentSlot.Type.ARMOR) {
            return this.inventory.armor.get(slot.getEntitySlotId());
        }
        return ItemStack.EMPTY;
    }

    public Scoreboard getScoreboard() {
        return this.getWorld().getScoreboard();
    }

    @Override
    public void equipStack(EquipmentSlot slot, ItemStack stack) {
        this.processEquippedStack(stack);
        if (slot == EquipmentSlot.MAINHAND) {
            this.onEquipStack(slot, this.inventory.main.set(this.inventory.selectedSlot, stack), stack);
        } else if (slot == EquipmentSlot.OFFHAND) {
            this.onEquipStack(slot, this.inventory.offHand.set(0, stack), stack);
        } else if (slot.getType() == EquipmentSlot.Type.ARMOR) {
            this.onEquipStack(slot, this.inventory.armor.set(slot.getEntitySlotId(), stack), stack);
        }
    }

    @Override
    public Arm getMainArm() {
        return Arm.RIGHT;
    }

    public Vec3d lerpVelocity(float tickDelta) {
        return this.lastVelocity.lerp(this.getVelocity(), tickDelta);
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        BlockPos bottomPos = this.getBlockPos().down();
        BlockState bottomBlockState = player.getWorld().getBlockState(bottomPos);
        Block bottomBlock = bottomBlockState.getBlock();


        if (bottomBlock instanceof DeathStatueBaseBlock) {
            return bottomBlock.onUse(bottomBlockState, player.getWorld(), bottomPos, player, hand, new BlockHitResult(this.getPos(), this.getHorizontalFacing(), this.getBlockPos(), false));
        }
        return super.interact(player, hand);
    }
}
