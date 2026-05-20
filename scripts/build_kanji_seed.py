#!/usr/bin/env python3
"""Build KotoBee kanji seed data from KANJIDIC2 and KanjiVG."""

from __future__ import annotations

import argparse
import gzip
import json
import unicodedata
import zipfile
from pathlib import Path
from typing import Iterable
from xml.etree import ElementTree as ET


RADICALS = (
    "一丨丶丿乙亅二亠人儿入八冂冖冫几凵刀力勹匕匚匸十卜卩厂厶又口囗土士夂"
    "夊夕大女子宀寸小尢尸屮山巛工己巾干幺广廴廾弋弓彐彡彳心戈戶手支攴"
    "文斗斤方无日曰月木欠止歹殳毋比毛氏气水火爪父爻爿片牙牛犬玄玉瓜瓦"
    "甘生用田疋疒癶白皮皿目矛矢石示禸禾穴立竹米糸缶网羊羽老而耒耳聿肉"
    "臣自至臼舌舛舟艮色艸虍虫血行衣襾見角言谷豆豕豸貝赤走足身車辛辰辵"
    "邑酉釆里金長門阜隶隹雨青非面革韋韭音頁風飛食首香馬骨高髟鬥鬯鬲鬼"
    "魚鳥鹵鹿麥麻黃黍黑黹黽鼎鼓鼠鼻齊齒龍龜龠"
)

OLD_JLPT_TO_N_LEVEL = {
    4: 5,
    3: 4,
    2: 3,
    1: 2,
}

LEVEL_ORDER = {
    5: 0,
    4: 1,
    3: 2,
    2: 3,
    1: 4,
    99: 5,
}


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--kanjidic", required=True, type=Path, help="Path to kanjidic2.xml or kanjidic2.xml.gz")
    parser.add_argument("--kanjivg-zip", required=True, type=Path, help="Path to a KanjiVG repository zip")
    parser.add_argument("--output", required=True, type=Path, help="Output JSON path")
    parser.add_argument("--limit", type=int, default=300, help="Number of kanji entries to export")
    return parser.parse_args()


def parse_int(value: str | None) -> int | None:
    if not value:
        return None
    try:
        return int(value)
    except ValueError:
        return None


def strip_namespace(tag: str) -> str:
    return tag.rsplit("}", 1)[-1]


def compact(values: Iterable[str | None]) -> list[str]:
    return [unicodedata.normalize("NFC", value.strip()) for value in values if value and value.strip()]


def load_svg_paths(kanjivg_zip_path: Path) -> dict[str, list[str]]:
    paths_by_codepoint: dict[str, list[str]] = {}

    with zipfile.ZipFile(kanjivg_zip_path) as archive:
        for file_info in archive.infolist():
            filename = file_info.filename.lower()
            if "/kanji/" not in filename or not filename.endswith(".svg"):
                continue

            codepoint = filename.rsplit("/", 1)[-1].removesuffix(".svg")
            root = ET.fromstring(archive.read(file_info))
            stroke_paths = [
                path_data.strip()
                for element in root.iter()
                if strip_namespace(element.tag) == "path"
                for path_data in [element.attrib.get("d")]
                if path_data and path_data.strip()
            ]

            if stroke_paths:
                paths_by_codepoint[codepoint] = stroke_paths

    return paths_by_codepoint


def open_kanjidic(path: Path):
    if path.suffix == ".gz":
        return gzip.open(path, "rb")
    return path.open("rb")


def radical_character(radical_number: int | None) -> str:
    if radical_number is None or radical_number < 1 or radical_number > len(RADICALS):
        return ""
    return RADICALS[radical_number - 1]


def mapped_jlpt_level(old_jlpt: int | None, grade: int | None) -> int:
    if old_jlpt in OLD_JLPT_TO_N_LEVEL:
        return OLD_JLPT_TO_N_LEVEL[old_jlpt]
    if grade in (1, 2):
        return 5
    if grade in (3, 4):
        return 4
    if grade in (5, 6):
        return 3
    if grade == 8:
        return 2
    return 99


def reading_values(rmgroup: ET.Element | None, reading_type: str) -> list[str]:
    if rmgroup is None:
        return []
    return compact(
        element.text
        for element in rmgroup.findall("reading")
        if element.attrib.get("r_type") == reading_type
    )


def english_meanings(rmgroup: ET.Element | None) -> list[str]:
    if rmgroup is None:
        return []
    return compact(
        element.text
        for element in rmgroup.findall("meaning")
        if element.attrib.get("m_lang", "en") == "en"
    )


def build_entries(kanjidic_path: Path, svg_paths: dict[str, list[str]]) -> list[dict[str, object]]:
    with open_kanjidic(kanjidic_path) as source:
        root = ET.parse(source).getroot()

    entries: list[dict[str, object]] = []

    for character in root.findall("character"):
        literal = (character.findtext("literal") or "").strip()
        if not literal:
            continue

        codepoint = f"{ord(literal):05x}"
        character_paths = svg_paths.get(codepoint)
        if not character_paths:
            continue

        misc = character.find("misc")
        stroke_count = parse_int(misc.findtext("stroke_count") if misc is not None else None) or len(character_paths)
        frequency = parse_int(misc.findtext("freq") if misc is not None else None)
        old_jlpt = parse_int(misc.findtext("jlpt") if misc is not None else None)
        grade = parse_int(misc.findtext("grade") if misc is not None else None)
        jlpt_level = mapped_jlpt_level(old_jlpt, grade)

        radical = character.find("radical")
        radical_number = None
        if radical is not None:
            for rad_value in radical.findall("rad_value"):
                if rad_value.attrib.get("rad_type") == "classical":
                    radical_number = parse_int(rad_value.text)
                    break

        rmgroup = character.find("reading_meaning/rmgroup")
        onyomi = reading_values(rmgroup, "ja_on")
        kunyomi = reading_values(rmgroup, "ja_kun")
        han_viet = reading_values(rmgroup, "vietnam")
        meanings_en = english_meanings(rmgroup)

        han_viet_text = ", ".join(han_viet).upper()
        meaning_en_text = "; ".join(meanings_en[:6])
        display_meaning = han_viet_text or meaning_en_text

        entries.append(
            {
                "character": literal,
                "kanji": literal,
                "meaning": display_meaning,
                "meaning_vi": display_meaning,
                "meaning_en": meaning_en_text,
                "hanViet": han_viet_text,
                "onyomi": "、".join(onyomi),
                "kunyomi": "、".join(kunyomi),
                "strokeCount": stroke_count,
                "strokes": stroke_count,
                "radical": radical_character(radical_number),
                "radical_number": radical_number,
                "jlptLevel": jlpt_level,
                "level": f"N{jlpt_level}" if jlpt_level != 99 else "",
                "frequency": frequency,
                "sort_order": 0,
                "status": "published",
                "svgPaths": character_paths,
            }
        )

    entries.sort(
        key=lambda entry: (
            LEVEL_ORDER.get(int(entry["jlptLevel"]), 5),
            entry["frequency"] if entry["frequency"] is not None else 999999,
            entry["strokeCount"],
            entry["character"],
        )
    )
    return entries


def main() -> None:
    args = parse_args()
    svg_paths = load_svg_paths(args.kanjivg_zip)
    entries = build_entries(args.kanjidic, svg_paths)[: args.limit]

    for index, entry in enumerate(entries, start=1):
        entry["sort_order"] = index

    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(
        json.dumps(entries, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )
    print(f"Wrote {len(entries)} kanji entries to {args.output}")


if __name__ == "__main__":
    main()
