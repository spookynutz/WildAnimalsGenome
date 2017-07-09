/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.wildAnimalsGenome;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.AliveCharacterComponent;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.delay.DelayedActionTriggeredEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.registry.In;
import org.terasology.wildAnimals.component.WildAnimalComponent;
import org.terasology.wildAnimalsGenome.component.MatingComponent;
import org.terasology.wildAnimalsGenome.event.MatingActivatedEvent;

import java.util.List;

@RegisterSystem
public class AnimalMatingSystem extends BaseComponentSystem {
    @In
    private DelayManager delayManager;

    @In
    private EntityManager entityManager;

    private long matingSearchInterval = 1000L;
    private float searchRadius = 10f;
    private static final String MATING_SEARCH_ID = "WildAnimalsGenome:MatingSearch";

    private static final Logger logger = LoggerFactory.getLogger(AnimalMatingSystem.class);

    @ReceiveEvent(components = {WildAnimalComponent.class})
    public void onMatingSearch(DelayedActionTriggeredEvent event, EntityRef entityRef, MatingComponent matingComponent) {
        if (matingComponent.readyToMate) {
            List<EntityRef> nearbyAnimals = findNearbyAnimals(entityRef.getComponent(LocationComponent.class), searchRadius, entityRef.getComponent(WildAnimalComponent.class).name);
            List<EntityRef> animals = filterMatingActivatedAnimals(nearbyAnimals);
            logger.info(animals.toString());
            delayManager.addDelayedAction(entityRef, MATING_SEARCH_ID, matingSearchInterval);
        }
    }

    @ReceiveEvent(components = {WildAnimalComponent.class})
    public void onMatingActivated(MatingActivatedEvent event, EntityRef entityRef, MatingComponent matingComponent) {
        delayManager.addDelayedAction(entityRef, MATING_SEARCH_ID, matingSearchInterval);
    }

    private List<EntityRef> findNearbyAnimals(LocationComponent actorLocationComponent, float searchRadius, String animalName) {
        List<EntityRef> animalsWithinRange = Lists.newArrayList();
        float maxDistanceSquared = searchRadius * searchRadius;
        Iterable<EntityRef> allAnimals = entityManager.getEntitiesWith(WildAnimalComponent.class);

        for (EntityRef animal : allAnimals) {
            LocationComponent animalLocationComponent = animal.getComponent(LocationComponent.class);
            if (animal.getComponent(AliveCharacterComponent.class) == null) {
                continue;
            }
            if (animalLocationComponent.getWorldPosition().distanceSquared(actorLocationComponent.getWorldPosition()) <= maxDistanceSquared) {
                if (animal.getComponent(WildAnimalComponent.class).name.equals(animalName)) {
                    animalsWithinRange.add(animal);
                }
            }
        }
        return animalsWithinRange;
    }

    private List<EntityRef> filterMatingActivatedAnimals(List<EntityRef> allAnimals) {
        List<EntityRef> result = Lists.newArrayList();
        for (EntityRef animal : allAnimals) {
            if (animal.hasComponent(MatingComponent.class) && animal.getComponent(MatingComponent.class).readyToMate) {
                result.add(animal);
            }
        }
        return result;
    }
}