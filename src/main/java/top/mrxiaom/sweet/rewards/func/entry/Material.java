package top.mrxiaom.sweet.rewards.func.entry;

import org.bukkit.inventory.ItemStack;
import top.mrxiaom.pluginbase.utils.ItemStackUtil;

public class Material {
    org.bukkit.Material material;
    Integer dataValue;
    Integer customModelData;
    public Material(org.bukkit.Material material, Integer dataValue, Integer customModelData) {
        this.material = material;
        this.dataValue = dataValue;
        this.customModelData = customModelData;
    }

    public ItemStack getNewItem() {
        ItemStack item = new ItemStack(material);
        if (dataValue != null) {
            item.setDurability(dataValue.shortValue());
        }
        if (customModelData != null) {
            ItemStackUtil.setCustomModelData(item, customModelData);
        }
        return item;
    }

    public Integer getCustomModelData() {
        return customModelData;
    }
}
