package com.ilmusu.musuen.registries;

import com.ilmusu.musuen.Resources;
import com.ilmusu.musuen.enchantments.*;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModEnchantments
{
    public static final Enchantment LACERATION = new LacerationEnchantment(Enchantment.Rarity.RARE);
    public static final Enchantment SKEWERING = new SkeweringEnchantment(Enchantment.Rarity.RARE);
    public static final Enchantment UNEARTHING = new UnearthingEnchantment(Enchantment.Rarity.RARE);
    public static final Enchantment PHASING = new PhasingEnchantment(Enchantment.Rarity.VERY_RARE);
    public static final Enchantment OVERCHARGED = new OverchargeEnchantment(Enchantment.Rarity.RARE);
    public static final Enchantment BERSERKER = new BerserkerEnchantment(Enchantment.Rarity.VERY_RARE);
    public static final Enchantment REACHING = new ReachingEnchantment(Enchantment.Rarity.UNCOMMON);
    public static final Enchantment POCKETED = new PocketedEnchantment(Enchantment.Rarity.UNCOMMON);
    public static final Enchantment VEIN_MINER = new VeinMinerEnchantment(Enchantment.Rarity.RARE);
    public static final Enchantment ATTRACTION = new AttractionEnchantment(Enchantment.Rarity.RARE);
    public static final Enchantment METEORITY = new MeteorityEnchantment(Enchantment.Rarity.UNCOMMON);
    public static final Enchantment WINGSPAN = new WingspanEnchantment(Enchantment.Rarity.COMMON);
    public static final Enchantment ALIGHTING = new AlightingEnchantment(Enchantment.Rarity.UNCOMMON);
    public static final Enchantment TELEKINESIS = new TelekinesisEnchantment(Enchantment.Rarity.RARE);
    public static final Enchantment SKY_JUMP = new SkyJumpEnchantment(Enchantment.Rarity.UNCOMMON);
    public static final Enchantment LONG_JUMP = new LongJumpEnchantment(Enchantment.Rarity.UNCOMMON);
    public static final Enchantment DEMONCTION = new DemonctionEnchantment(Enchantment.Rarity.RARE);
    public static final Enchantment SKYHOOK = new SkyhookEnchantment(Enchantment.Rarity.VERY_RARE);
    public static final Enchantment REFLECTION = new ReflectionEnchantment(Enchantment.Rarity.COMMON);
    public static final Enchantment SHOCKWAVE = new ShockwaveEnchantment(Enchantment.Rarity.UNCOMMON);
    public static final Enchantment COVERAGE = new CoverageEnchantment(Enchantment.Rarity.UNCOMMON);
    public static final Enchantment EVERLASTING = new EverlastingEnchantment(Enchantment.Rarity.UNCOMMON);
    public static final Enchantment ZERO_GRAVITY = new ZeroGravityEnchantment(Enchantment.Rarity.UNCOMMON);
    public static final Enchantment DREAMLIKE = new DreamlikeEnchantment(Enchantment.Rarity.RARE);
    public static final Enchantment GLUTTONY = new GluttonyEnchantment(Enchantment.Rarity.VERY_RARE);
    public static final Enchantment MULTI_ARROW = new MultiArrowEnchantment(Enchantment.Rarity.VERY_RARE);
    public static final Enchantment GUILLOTINING = new GuillotiningEnchantment(Enchantment.Rarity.VERY_RARE);
    public static final Enchantment SCYTHING = new ScythingEnchantment(Enchantment.Rarity.RARE);

    public static void register()
    {
        registerEnchantmentIfEnabled("laceration", LACERATION);
        registerEnchantmentIfEnabled("skewering", SKEWERING);
        registerEnchantmentIfEnabled("unearthing", UNEARTHING);
        registerEnchantmentIfEnabled("phasing", PHASING);
        registerEnchantmentIfEnabled("overcharged", OVERCHARGED);
        registerEnchantmentIfEnabled("berserker", BERSERKER);
        registerEnchantmentIfEnabled("guillotining", GUILLOTINING);
        registerEnchantmentIfEnabled("reaching", REACHING);
        registerEnchantmentIfEnabled("pocketed", POCKETED);
        registerEnchantmentIfEnabled("vein_miner", VEIN_MINER);
        registerEnchantmentIfEnabled("attraction", ATTRACTION);
        registerEnchantmentIfEnabled("meteority", METEORITY);
        registerEnchantmentIfEnabled("wingspan", WINGSPAN);
        registerEnchantmentIfEnabled("alighting", ALIGHTING);
        registerEnchantmentIfEnabled("telekinesis", TELEKINESIS);
        registerEnchantmentIfEnabled("sky_jump", SKY_JUMP);
        registerEnchantmentIfEnabled("long_jump", LONG_JUMP);
        registerEnchantmentIfEnabled("demonction", DEMONCTION);
        registerEnchantmentIfEnabled("skyhook", SKYHOOK);
        registerEnchantmentIfEnabled("reflection", REFLECTION);
        registerEnchantmentIfEnabled("shockwave", SHOCKWAVE);
        registerEnchantmentIfEnabled("coverage", COVERAGE);
        registerEnchantmentIfEnabled("everlasting", EVERLASTING);
        registerEnchantmentIfEnabled("zero_gravity", ZERO_GRAVITY);
        registerEnchantmentIfEnabled("dreamlike", DREAMLIKE);
        registerEnchantmentIfEnabled("gluttony", GLUTTONY);
        registerEnchantmentIfEnabled("multi_arrow", MULTI_ARROW);
        registerEnchantmentIfEnabled("scything", SCYTHING);
    }

    public static void registerEnchantmentIfEnabled(String name, Enchantment enchantment)
    {
        // Registering the configs and checking if enabled
        if(!ModConfigurations.registerEnchantmentConfig(name))
            return;
        // Registering the enchantment and forcing the registration of the levels
        Registry.register(Registries.ENCHANTMENT, Resources.identifier(name), enchantment);
        // Registering the min and max levels for the enchantment
        enchantment.getMinLevel();
        enchantment.getMaxLevel();
    }
}