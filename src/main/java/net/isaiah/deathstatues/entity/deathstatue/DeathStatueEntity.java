package net.isaiah.deathstatues.entity.deathstatue;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.isaiah.deathstatues.block.statue.DeathStatueBaseBlock;
import net.isaiah.deathstatues.item.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.entity.PlayerModelPart;
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
import net.minecraft.text.Style;
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
    private Identifier skinTexture;
    private boolean playerListEntrySet;

    public DeathStatueEntity(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
        this.playerListEntrySet = false;
    }

    public static DeathStatueEntity create(EntityType<? extends LivingEntity> entityType, World world) {
        return new DeathStatueEntity(entityType, world);
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
            if (this.getWorld().isClient()) {
                this.playerListEntry = Objects.requireNonNull(MinecraftClient.getInstance().getNetworkHandler()).getPlayerListEntry(getPlayerUUIDFromStatueName(this.getName().getString()));
            }
        }
        else {
            System.out.println("Player List Entry: " + this.playerListEntry.getProfile().getName());
        }
        return this.playerListEntry;
    }

    @Environment(value=EnvType.CLIENT)
    public static UUID getPlayerUUIDFromStatueName(String entityName) {
        //Gets characters from between two square brackets, "[ ]"
        Pattern pattern = Pattern.compile("\\[(.*?)]");
        Matcher matcher = pattern.matcher(entityName);
        String uuidString = "uuidString";
        String playerName = "playerName";

        if (matcher.find() || !entityName.contains("[")) {
            try {
                if (!entityName.contains("[")) {
                    playerName = entityName;
                }
                else if (matcher.group(1) != null) {
                    playerName = matcher.group(1);
                }

                if (!playerName.toLowerCase().contains("player")) {
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
        if (!this.playerListEntrySet) {
            this.playerListEntry = this.getPlayerListEntry();
            if (this.playerListEntry != null) {
                this.skinTexture = new Identifier(this.playerListEntry.getSkinTexture().toTranslationKey().replace("minecraft.", ""));
            }
            this.playerListEntrySet = true;
        }
        if (this.playerListEntry == null) {
            return DefaultSkinHelper.getTexture(Objects.requireNonNull(getPlayerUUIDFromStatueName(this.getName().getString())));
        } else {
            //System.out.println("Player List Entry Path: " + this.playerListEntry.getSkinTexture().toTranslationKey().replace("minecraft.", ""));
            //Identifier skinIdentifier = new Identifier(this.playerListEntry.getSkinTexture().toTranslationKey().replace("minecraft.", ""));
            //System.out.println("Skin Identifier: " + skinIdentifier.toTranslationKey());
            return this.skinTexture;
            //return this.playerListEntry.getSkinTexture();
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

        if (player.isSneaking() && hand.equals(Hand.MAIN_HAND)) {
            /*if (!hasCustomStatueItem(player)) {
                ItemStack deathStatueStack = ModItems.DEATH_STATUE_ITEM.getDefaultStack();
                NbtCompound nbtData = new NbtCompound();
                nbtData.put("Inventory", this.inventory.writeNbt(new NbtList()));
                System.out.println("NBT DATA: " + nbtData);
                deathStatueStack.setNbt(nbtData);
                deathStatueStack.setCustomName(this.getCustomName());
                player.getInventory().insertStack(deathStatueStack);
                this.remove(RemovalReason.KILLED);
                //player.getInventory().insertStack(ModItems.DEATH_STATUE_ITEM.getDefaultStack().setCustomName(this.getCustomName()));
            }*/
            ItemStack deathStatueStack = ModItems.DEATH_STATUE_ITEM.getDefaultStack();
            NbtCompound nbtData = new NbtCompound();
            nbtData.put("Inventory", this.inventory.writeNbt(new NbtList()));
            deathStatueStack.setNbt(nbtData);
            deathStatueStack.setCustomName(Objects.requireNonNull(this.getCustomName()).copyContentOnly().setStyle(Style.EMPTY.withColor(Formatting.DARK_PURPLE)));
            player.getInventory().insertStack(deathStatueStack);
            this.remove(RemovalReason.KILLED);
        }
        else if (bottomBlock instanceof DeathStatueBaseBlock) {
            return bottomBlock.onUse(bottomBlockState, player.getWorld(), bottomPos, player, hand, new BlockHitResult(this.getPos(), this.getHorizontalFacing(), this.getBlockPos(), false));
        }
        return super.interact(player, hand);
    }

    public boolean hasCustomStatueItem(PlayerEntity player) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            NbtCompound displayTag = stack.getOrCreateSubNbt("display");

            if (displayTag.contains("Name", 8)) {
                String displayName = displayTag.getString("Name");
                //System.out.println("Display Name: "+ displayName);
                if (displayName != null && displayName.contains(this.getDisplayName().getString())) {
                    System.out.println("Player has custom statue item already");
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isPartVisible(PlayerModelPart modelPart) {
        return (this.getDataTracker().get(PLAYER_MODEL_PARTS) & modelPart.getBitFlag()) == modelPart.getBitFlag();
    }
}
