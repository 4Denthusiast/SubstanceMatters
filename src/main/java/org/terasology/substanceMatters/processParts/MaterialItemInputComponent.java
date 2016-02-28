/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.substanceMatters.processParts;

import com.google.common.base.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.utilities.Assets;
import org.terasology.assets.ResourceUrn;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.inventory.InventoryUtils;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.substanceMatters.components.MaterialCompositionComponent;
import org.terasology.workstation.process.inventory.InventoryInputComponent;
import org.terasology.workstation.process.inventory.InventoryInputProcessPartSlotAmountsComponent;
import org.terasology.workstation.process.inventory.ItemPrefabOutputComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MaterialItemInputComponent extends InventoryInputComponent {
    private static final Logger logger = LoggerFactory.getLogger(MaterialItemInputComponent.class);

    public Map<String, Integer> itemCounts;

    @Override
    public boolean validateBeforeStart(EntityRef instigator, EntityRef workstation, EntityRef processEntity) {
        boolean isValid = super.validateBeforeStart(instigator, workstation, processEntity);

        // extract all substances to the process entity
        if (isValid) {
            MaterialCompositionComponent materialCompositionComponent = processEntity.getComponent(MaterialCompositionComponent.class);
            if (materialCompositionComponent == null) {
                materialCompositionComponent = new MaterialCompositionComponent();
            }

            InventoryInputProcessPartSlotAmountsComponent inputItems = processEntity.getComponent(InventoryInputProcessPartSlotAmountsComponent.class);
            for (Map.Entry<Integer, Integer> slotAmount : inputItems.slotAmounts.entrySet()) {
                EntityRef item = InventoryUtils.getItemAt(workstation, slotAmount.getKey());
                materialCompositionComponent.addMaterialFromItem(item, slotAmount.getValue());
            }

            if (processEntity.hasComponent(MaterialCompositionComponent.class)) {
                processEntity.saveComponent(materialCompositionComponent);
            } else {
                processEntity.addComponent(materialCompositionComponent);
            }
        }

        return isValid;
    }

    @Override
    protected Map<Predicate<EntityRef>, Integer> getInputItems() {
        Map<Predicate<EntityRef>, Integer> result = new HashMap<>();
        for (Map.Entry<String, Integer> itemCount : itemCounts.entrySet()) {
            try {
                result.put(new ItemPrefabPredicate(Assets.getPrefab(itemCount.getKey()).get().getUrn()), itemCount.getValue());
            } catch (Exception ex) {
                logger.error("Bad item: " + itemCount.getKey());
                throw ex;
            }
        }

        return result;
    }

    @Override
    protected Set<EntityRef> createItems() {
        return ItemPrefabOutputComponent.createOutputItems(itemCounts, false);
    }

    private static final class ItemPrefabPredicate implements Predicate<EntityRef> {
        private ResourceUrn prefab;

        private ItemPrefabPredicate(ResourceUrn prefab) {
            this.prefab = prefab;
        }

        @Override
        public boolean apply(EntityRef input) {
            ItemComponent item = input.getComponent(ItemComponent.class);
            if (item == null) {
                return false;
            }
            String inputItemUri = input.getParentPrefab().getName();
            MaterialCompositionComponent materialCompositionComponent = input.getComponent(MaterialCompositionComponent.class);
            if (materialCompositionComponent != null && materialCompositionComponent.hasSubstance()) {
                inputItemUri += "#" + materialCompositionComponent.getPrimarySubstance();
            }

            return inputItemUri.toLowerCase().startsWith(prefab.toString().toLowerCase());
        }
    }
}
