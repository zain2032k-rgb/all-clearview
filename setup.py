# Sorts the flat uploaded files into the folders the build needs, and makes the totem textures.
import os, re, shutil, zipfile
from PIL import Image

R = "src/main/resources"
for name in sorted(os.listdir(".")):
    if name.endswith(".java"):
        text = open(name, encoding="utf-8").read()
        pkg = re.search(r"^package\s+([\w.]+);", text, re.M).group(1)
        dest = "src/main/java/" + pkg.replace(".", "/")
        os.makedirs(dest, exist_ok=True)
        shutil.move(name, os.path.join(dest, name))

RES = {
    "clearview.mixins.json": R,
    "fabric.mod.json": R,
    "en_us.json": R + "/assets/clearview/lang",
}
for name, dest in RES.items():
    if os.path.exists(name):
        os.makedirs(dest, exist_ok=True)
        shutil.move(name, os.path.join(dest, name))

# recorded keyboard + mouse samples (one zip so the upload stays small)
if os.path.exists("keysamples.zip"):
    zipfile.ZipFile("keysamples.zip").extractall(R + "/assets/clearview")

LEFT = [
 "........", ".....ooo", "....oLLL", "....oBEB", "....oBBB", ".....oBB",
 "..oooBBD", ".oBBoBGG", ".oBBoBGG", "..oooBBD", ".....oBB", ".....oBD",
 "....oBBD", "....oDDD", "....oooo", "........",
]
PAL = {
 "gold":     ((92,58,10),(230,170,40),(255,225,110),(170,115,20),(30,30,30),(60,200,90)),
 "ruby":     ((70,10,20),(190,30,50),(240,110,120),(120,15,30),(255,230,230),(255,215,80)),
 "sapphire": ((10,25,70),(40,90,200),(120,170,255),(25,55,140),(240,250,255),(120,255,255)),
 "emerald":  ((10,60,30),(40,170,80),(130,235,150),(25,110,55),(10,30,15),(255,230,90)),
 "amethyst": ((45,15,80),(140,70,210),(200,150,255),(90,40,150),(255,240,255),(255,120,220)),
 "ice":      ((40,80,110),(160,220,240),(235,252,255),(110,170,200),(30,60,90),(80,140,255)),
 "shadow":   ((8,8,12),(55,55,70),(105,105,125),(30,30,40),(255,60,60),(160,60,255)),
 "sakura":   ((110,40,70),(250,160,190),(255,215,230),(215,110,150),(90,30,60),(255,255,255)),
 "lime":     ((40,80,10),(150,220,40),(210,255,120),(100,160,20),(25,50,5),(255,120,40)),
 "ember":    ((60,15,5),(235,100,25),(255,185,70),(170,60,15),(255,240,160),(255,60,20)),
 "ghost":    ((120,125,140),(235,238,250),(255,255,255),(190,195,215),(60,60,90),(150,200,255)),
 "copper":   ((70,35,15),(200,115,70),(240,170,120),(140,75,40),(20,40,35),(60,190,170)),
}
for name, (o, b, l, d, e, g) in PAL.items():
    m = {"o": o, "B": b, "L": l, "D": d, "E": e, "G": g}
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(LEFT):
        for x, ch in enumerate(row + row[::-1]):
            if ch in m:
                img.putpixel((x, y), m[ch] + (255,))
    folder = f"{R}/resourcepacks/totem_{name}/assets/minecraft/textures/item"
    os.makedirs(folder, exist_ok=True)
    img.save(f"{folder}/totem_of_undying.png")
print("setup done")
