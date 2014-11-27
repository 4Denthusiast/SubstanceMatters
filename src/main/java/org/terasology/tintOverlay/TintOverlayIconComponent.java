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
package org.terasology.tintOverlay;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;
import org.terasology.entitySystem.Component;
import org.terasology.reflection.MappedContainer;

import java.util.Map;

public class TintOverlayIconComponent implements Component {
    public Map<String, TintParameter> texture = Maps.newHashMap();

    public TintParameter getTintParameterForIcon(String iconUri) {
        for (Map.Entry<String, TintParameter> overlayItem : texture.entrySet()) {
            AssetUri toolItemIcon = Assets.resolveAssetUri(AssetType.SUBTEXTURE, overlayItem.getKey());
            AssetUri inputItemIcon = Assets.resolveAssetUri(AssetType.SUBTEXTURE, iconUri);
            if (toolItemIcon.equals(inputItemIcon)) {
                return overlayItem.getValue();
            }
        }

        return null;
    }

    @MappedContainer
    public static class TintParameter {
        private static final String DELIMITER = "~";

        /**
         * Change the hue to this value in between 0 and 360
         */
        public Integer hue;
        /**
         * Scale the brightness value so that texture is maintained after the hue replacement
         */
        public float brightnessScale = 1.0f;
        /**
         * Scale the saturation value so that texture is maintained after the hue replacement
         */
        public float saturationScale = 1.0f;
        /**
         * positive values shift right, negative values shift left
         */
        public int shiftX = 0;
        /**
         * positive values shift down, negative values shift up
         */
        public int shiftY = 0;

        public boolean invisible = false;

        public TintParameter(Integer hue, float brightnessScale, float saturationScale, int shiftX, int shiftY) {
            this.hue = hue;
            this.brightnessScale = brightnessScale;
            this.saturationScale = saturationScale;
            this.shiftX = shiftX;
            this.shiftY = shiftY;
        }

        public TintParameter() {
        }

        TintParameter(String delimitedString) {
            String[] split = delimitedString.split(DELIMITER);
            if (split.length == 5) {
                if (!split[0].isEmpty()) {
                    hue = Integer.parseInt(split[0]);
                }
                brightnessScale = Float.parseFloat(split[1]);
                saturationScale = Float.parseFloat(split[2]);
                shiftX = Integer.parseInt(split[3]);
                shiftY = Integer.parseInt(split[4]);
            }
        }

        String toDelimitedString() {
            return Joiner.on(DELIMITER).join(new String[]{
                    hue == null ? "" : String.valueOf(hue),
                    String.valueOf(brightnessScale),
                    String.valueOf(saturationScale),
                    String.valueOf(shiftX),
                    String.valueOf(shiftY)
            });
        }

        /**
         * @return A float hue in between 0-1 based on the 360 different hues
         */
        float getScaledHue() {

            return (float) hue / 360f;
        }
    }
}
