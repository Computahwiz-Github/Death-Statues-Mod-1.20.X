package net.isaiah.deathstatues.entity.deathstatue;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.isaiah.deathstatues.block.statue.DeathStatueBaseBlock;
import net.isaiah.deathstatues.item.ModItems;
import net.isaiah.deathstatues.screen.DeathStatuesScreen;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
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
        String uuidString;
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

    public void giveCustomStatueItem(PlayerEntity player) {
        ItemStack deathStatueStack = ModItems.DEATH_STATUE_ITEM.getDefaultStack();
        NbtCompound nbtData = new NbtCompound();
        nbtData.put("Inventory", this.inventory.writeNbt(new NbtList()));
        //System.out.println("NBT DATA: " + nbtData);
        deathStatueStack.setNbt(nbtData);
        String customName = Objects.requireNonNull(this.getCustomName()).getString();

        Pattern pattern = Pattern.compile("\\[(.*?)]");
        Matcher matcher = pattern.matcher(customName);
        if (matcher.find()) {
            try {
                if (matcher.group(1) != null) {
                    customName = matcher.group(1);
                    deathStatueStack.setCustomName(Text.of("Death Statue of ").copyContentOnly().formatted(Formatting.AQUA)
                            .append(Text.of("[").copyContentOnly().formatted(Formatting.GREEN))
                            .append(Text.of(customName).copyContentOnly().formatted(Formatting.GOLD))
                            .append(Text.of("]").copyContentOnly().formatted(Formatting.GREEN)));
                }
            } catch (IllegalArgumentException e) {
                // UUID parsing failed
                e.printStackTrace();
            }
        }
        this.remove(RemovalReason.KILLED);
        player.getInventory().insertStack(deathStatueStack);
    }

    @Override
    public ActionResult interactAt(PlayerEntity player, Vec3d hitPos, Hand hand) {
        BlockPos bottomPos = this.getBlockPos().down();
        BlockState bottomBlockState = player.getWorld().getBlockState(bottomPos);
        Block bottomBlock = bottomBlockState.getBlock();

        if (!player.isSneaking() && hand.equals(Hand.MAIN_HAND)) {
            if (bottomBlock instanceof DeathStatueBaseBlock) {
                DeathStatuesScreen.setDeathStatueEntityID(this.getId());
                return bottomBlock.onUse(bottomBlockState, player.getWorld(), bottomPos, player, hand, new BlockHitResult(this.getPos(), this.getHorizontalFacing(), this.getBlockPos(), false));
            }
        }

        ItemStack itemStack = player.getStackInHand(hand);
        if (player.isSpectator()) {
            return ActionResult.SUCCESS;
        }
        if (player.getWorld().isClient) {
            return ActionResult.CONSUME;
        }

        EquipmentSlot equipmentSlot = MobEntity.getPreferredEquipmentSlot(itemStack);
        if (itemStack.isEmpty()) {
            if (player.isSneaking() && hand.equals(Hand.MAIN_HAND) && Screen.hasControlDown()) {
                giveCustomStatueItem(player);
                return ActionResult.SUCCESS;
            }
            EquipmentSlot equipmentSlot2 = this.getSlotFromPosition(hitPos);
            if (this.hasStackEquipped(equipmentSlot2) && this.equip(player, equipmentSlot2, itemStack, hand)) {
                return ActionResult.SUCCESS;
            }
        }
        else {
            if (this.equip(player, equipmentSlot, itemStack, hand)) {
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS;
    }

    private EquipmentSlot getSlotFromPosition(Vec3d hitPos) {
        EquipmentSlot equipmentSlot = EquipmentSlot.MAINHAND;
        double d = hitPos.y;
        EquipmentSlot equipmentSlot2 = EquipmentSlot.FEET;
        if (d >= 0.1) {
            double d2 = 0.45;
            if (d < 0.1 + d2 && this.hasStackEquipped(equipmentSlot2)) {
                return EquipmentSlot.FEET;
            }
        }
        double d3 = 0.0;
        if (d >= 0.9 + d3) {
            double d4 = 0.7;
            if (d < 0.9 + d4 && this.hasStackEquipped(EquipmentSlot.CHEST)) {
                return EquipmentSlot.CHEST;
            }
        }
        if (d >= 0.4) {
            double d5 = 0.8;
            if (d < 0.4 + d5 && this.hasStackEquipped(EquipmentSlot.LEGS)) {
                return EquipmentSlot.LEGS;
            }
        }
        if (d >= 1.6 && this.hasStackEquipped(EquipmentSlot.HEAD)) {
            return EquipmentSlot.HEAD;
        }
        if (this.hasStackEquipped(EquipmentSlot.MAINHAND)) return equipmentSlot;
        if (!this.hasStackEquipped(EquipmentSlot.OFFHAND)) return equipmentSlot;
        return EquipmentSlot.OFFHAND;
    }

    private boolean equip(PlayerEntity player, EquipmentSlot slot, ItemStack stack, Hand hand) {
        ItemStack itemStack = this.getEquippedStack(slot);
        if (player.getAbilities().creativeMode && itemStack.isEmpty() && !stack.isEmpty()) {
            this.equipStack(slot, stack.copyWithCount(1));
            return true;
        }
        if (!stack.isEmpty() && stack.getCount() > 1) {
            if (!itemStack.isEmpty()) {
                return false;
            }
            this.equipStack(slot, stack.split(1));
            return true;
        }
        this.equipStack(slot, stack);
        player.setStackInHand(hand, itemStack);
        if (!itemStack.isEmpty()) {
            this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, this.getSoundCategory(), 1.0f, this.getWorld().random.nextFloat() * 0.1f + 0.9f);
        }
        return true;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        ItemStack itemStack;
        int i;
        if (source.getAttacker() != null) {
            source.getAttacker().playSound(SoundEvents.ENTITY_ITEM_BREAK, 1.0f, this.getWorld().random.nextFloat() * 0.1f + 0.9f);
            if (!source.isSourceCreativePlayer()) {
                Block.dropStack(this.getWorld(), this.getBlockPos(), ModItems.DEATH_STATUE_ITEM.getDefaultStack());
                for (i = 0; i < this.inventory.main.size(); ++i) {
                    itemStack = this.inventory.main.get(i);
                    if (itemStack.isEmpty()) continue;
                    this.dropStack(itemStack);
                    this.inventory.main.set(i, ItemStack.EMPTY);
                }
                for (i = 0; i < this.inventory.offHand.size(); ++i) {
                    itemStack = this.inventory.offHand.get(i);
                    if (itemStack.isEmpty()) continue;
                    this.dropStack(itemStack);
                    this.inventory.offHand.set(i, ItemStack.EMPTY);
                }
                for (i = 0; i < this.inventory.armor.size(); ++i) {
                    itemStack = this.inventory.armor.get(i);
                    if (itemStack.isEmpty()) continue;
                    this.dropStack(itemStack);
                    this.inventory.armor.set(i, ItemStack.EMPTY);
                }
            }
        }
        return super.damage(source, amount);
    }
}
