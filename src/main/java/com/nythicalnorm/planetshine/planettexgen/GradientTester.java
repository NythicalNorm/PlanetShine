package com.nythicalnorm.planetshine.planettexgen;

import java.awt.*;

public class GradientTester {
    public static PlanetGradient STAR_GRADIENT = new PlanetGradient(new BiomeGroup[]{
            new BiomeGroup("orangeHot", -0.5f, 0.5f, new BiomeGradient[]{
                        new BiomeGradient(0,0.5f, 0, 1, 1f, Color.decode("#fbba39")),
                    new BiomeGradient(0.5f,0.6f, 0, 1, 1f, Color.decode("#fbd955")),
                    new BiomeGradient(0.6f,0.72f, 0, 1, 1f, Color.decode("#ffffa8")),
                    new BiomeGradient(0.70f,1f, 0, 1, 1f, Color.decode("#fffffe")),
            }),
    });

    public static PlanetGradient OVERWORLD_GRADIENT = new PlanetGradient(new BiomeGroup[]{
            new BiomeGroup("Ocean", -0.6f, 0.1f, new BiomeGradient[]{
                    new BiomeGradient(0,0.85f, 0, 1, 1f, Color.decode("#3938C9")),
                    new BiomeGradient(0.85f,1f, 0, 1, 1f, Color.decode("#3D57D6")),
            }),
            new BiomeGroup("Land", 0.1f, 0.5f, new BiomeGradient[]{
                    new BiomeGradient(0f,0.4f, 0, 1f, 1f, Color.decode("#71A74D")),
                    new BiomeGradient(0.4f,0.75f, 0, 1f, 1f, Color.decode("#737373")),
                    new BiomeGradient(0.75f,1f, 0.78f, 1, 0.5f, Color.decode("#ffffff")),
            }),
    });

    public static PlanetGradient NILA_GRADIENT = new PlanetGradient(new BiomeGroup[]{
            new BiomeGroup("Mare", -0.5f, -0.2f, new BiomeGradient[]{
                    new BiomeGradient(0f,0.5f, 0, 1, 1f, Color.decode("#5d5d5d")),
                    new BiomeGradient(0.5f,0.90f, 0, 1, 1f, Color.decode("#3c3c3c")),
                    new BiomeGradient(0.90f,1f, 0, 1, 1f, Color.decode("#595959")),
            }),

            new BiomeGroup("Land", -0.2f, 0.5f, new BiomeGradient[]{
                    new BiomeGradient(0f,0.15f, 0, 1f, 1f, Color.decode("#959293")),
                    new BiomeGradient(0.15f,0.3f, 0, 1f, 1f, Color.decode("#c3c1c2")),
                    new BiomeGradient(0.3f,1f, 0, 1f, 1f, Color.decode("#d0d0d0")),
            }),
    });

    public static PlanetGradient MARS_GRADIENT = new PlanetGradient(new BiomeGroup[] {
            new BiomeGroup("high_lands", -0.5f, 0.2f, new BiomeGradient[]{
                    new BiomeGradient(0f,0.4f, 0, 1, 1f, Color.decode("#fe844e")),
                    new BiomeGradient(0.4f,0.7f, 0, 1, 1f, Color.decode("#fa7241")),
                    new BiomeGradient(0.7f,1f, 0, 1, 1f, Color.decode("#f16a3d")),
            }),

            new BiomeGroup("low_lands", 0.2f, 0.5f, new BiomeGradient[]{
                    new BiomeGradient(0f,0.2f, 0, 1f, 1f, Color.decode("#e3b366")),
                    new BiomeGradient(0.2f,0.3f, 0, 1f, 1f, Color.decode("#df623c")),
                    new BiomeGradient(0.3f,1f, 0, 1f, 1f, Color.decode("#da633a")),
            }),
    });

    public static PlanetGradient VENUS_GRADIENT = new PlanetGradient(new BiomeGroup[] {
            new BiomeGroup("light_clouds", -0.5f, 0.25f, new BiomeGradient[]{
                    new BiomeGradient(0f,0.6f, 0, 1, 1f, Color.decode("#d1a662")),
                    new BiomeGradient(0.6f,0.7f, 0, 1, 1f, Color.decode("#c59c58")),
                    new BiomeGradient(0.7f,1f, 0, 1, 1f, Color.decode("#c59c58")),
            }),

            new BiomeGroup("dark_clouds", 0.10f, 0.5f, new BiomeGradient[]{
                    new BiomeGradient(0f,0.6f, 0, 1f, 1f, Color.decode("#bd8e4b")),
                    new BiomeGradient(0.6f,0.75f, 0, 1f, 1f, Color.decode("#ad8453")),
                    new BiomeGradient(0.75f,1f, 0, 1f, 1f, Color.decode("#81613c")),
            }),
    });

    public static PlanetGradient MERCURY_GRADIENT = new PlanetGradient(new BiomeGroup[] {
            new BiomeGroup("land", -0.5f, 0.2f, new BiomeGradient[]{
                    new BiomeGradient(0f,0.5f, 0, 1, 1f, Color.decode("#4c293b")),
                    new BiomeGradient(0.5f,0.7f, 0, 1, 1f, Color.decode("#713d48")),
                    new BiomeGradient(0.7f,1f, 0, 1, 1f, Color.decode("#824650")),
            }),

            new BiomeGroup("lava", 0.2f, 0.5f, new BiomeGradient[]{
                    new BiomeGradient(0f,0.4f, 0, 1f, 1f, Color.decode("#d45609")),
                    new BiomeGradient(0.4f,1.0f, 0, 1f, 1f, Color.decode("#da7b21")),
                    //new BiomeGradient(0.75f,1f, 0, 1f, 1f, Color.decode("#81613c")),
            }),
    });
}
