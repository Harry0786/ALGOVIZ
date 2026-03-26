import json
import re
import urllib.request
from collections import defaultdict
from pathlib import Path

OUT = Path("app/src/main/java/com/algoviz/plus/ui/learn/data/CheatSheetCatalog.kt")


def fetch_json(url: str):
    with urllib.request.urlopen(url, timeout=30) as r:
        return json.loads(r.read().decode("utf-8"))


def fetch_text(url: str):
    with urllib.request.urlopen(url, timeout=30) as r:
        return r.read().decode("utf-8", errors="ignore")


def clean_title(raw: str) -> str:
    s = raw
    s = re.sub(r"\.[a-zA-Z0-9]+$", "", s)
    s = s.replace("_", " ").replace("-", " ")
    s = s.replace("&", " and ")
    s = re.sub(r"^\d+\.?\s*", "", s)
    s = re.sub(r"\s+", " ", s).strip(" .")
    return s.title() if s else "Problem"


def clean_section(raw: str) -> str:
    s = raw
    s = s.replace("_", " ").replace("-", " ")
    s = re.sub(r"^\d+\.?\s*", "", s)
    s = re.sub(r"\s+", " ", s).strip(" .")
    return s.title() if s else "General"


def tag_for_section(section: str) -> str:
    sec = section.lower()
    if "graph" in sec:
        return "GRAPH"
    if "sort" in sec:
        return "SORTING"
    if "search" in sec:
        return "SEARCHING"
    if "dp" in sec or "dynamic" in sec:
        return "DYNAMIC_PROGRAMMING"
    if "recursion" in sec or "backtrack" in sec:
        return "RECURSION"
    return "DATA_STRUCTURES"


def parse_tree_sheet(tree_url: str, item_prefix: str):
    data = fetch_json(tree_url)
    sections = defaultdict(list)
    for node in data.get("tree", []):
        path = node.get("path", "")
        typ = node.get("type", "")
        if typ != "blob":
            continue
        if not re.search(r"\.(cpp|cc|cxx|java|py|js|ts|kt)$", path):
            continue
        parts = path.split("/")
        if len(parts) < 2:
            continue
        sec = clean_section(parts[0])
        title = clean_title(parts[-1])
        if title.lower() in {"readme", "main"}:
            continue
        sections[sec].append(title)

    out = []
    counter = 1
    for sec in sorted(sections.keys()):
        seen = set()
        items = []
        for title in sorted(sections[sec]):
            key = title.lower()
            if key in seen:
                continue
            seen.add(key)
            item_id = f"{item_prefix}_{counter:04d}"
            counter += 1
            tag = tag_for_section(sec)
            items.append(
                {
                    "id": item_id,
                    "title": title,
                    "explanation": f"Practice question from the {sec} section.",
                    "keyPoints": [
                        "Solve with brute force first",
                        "Optimize using standard pattern",
                        "Review edge cases and constraints",
                    ],
                    "tag": tag,
                }
            )
        if items:
            out.append((sec, items))
    return out


def parse_neetcode_markdown(md_url: str):
    text = fetch_text(md_url)
    sections = defaultdict(list)
    current = None
    for line in text.splitlines():
        h = re.match(r"^####\s+(.+?)\s*$", line.strip())
        if h:
            current = h.group(1).strip()
            continue
        if not current:
            continue
        m = re.match(r"^\d+\.\s+\[(.+?)\]\((.+?)\)", line.strip())
        if m:
            title = clean_title(m.group(1))
            sections[current].append(title)
            continue
        m2 = re.match(r"^\d+\.\s+(.+)$", line.strip())
        if m2:
            title = clean_title(m2.group(1))
            sections[current].append(title)

    out = []
    counter = 1
    for sec in sorted(sections.keys()):
        seen = set()
        items = []
        for title in sections[sec]:
            key = title.lower()
            if key in seen:
                continue
            seen.add(key)
            item_id = f"nt_{counter:04d}"
            counter += 1
            tag = tag_for_section(sec)
            items.append(
                {
                    "id": item_id,
                    "title": title,
                    "explanation": f"Practice problem from NeetCode {sec}.",
                    "keyPoints": [
                        "Identify the dominant pattern",
                        "Target optimal time complexity",
                        "Validate with dry runs",
                    ],
                    "tag": tag,
                }
            )
        if items:
            out.append((clean_section(sec), items))
    return out


def esc(s: str) -> str:
    return s.replace("\\", "\\\\").replace('"', '\\"')


def emit_item(it, indent=" " * 24):
    kp = ",\n".join([f'{indent}            "{esc(k)}"' for k in it["keyPoints"]])
    return (
        f"{indent}LearnItem(\n"
        f"{indent}    id = \"{esc(it['id'])}\",\n"
        f"{indent}    title = \"{esc(it['title'])}\",\n"
        f"{indent}    explanation = \"{esc(it['explanation'])}\",\n"
        f"{indent}    keyPoints = listOf(\n{kp}\n{indent}    ),\n"
        f"{indent}    tags = setOf(LearnTopicTag.{it['tag']})\n"
        f"{indent})"
    )


def emit_sheet(sheet_id, influencer, title, note, sections, sec_prefix):
    out = []
    out.append("        LearnSheet(")
    out.append(f'            id = "{esc(sheet_id)}",')
    out.append(f'            influencer = "{esc(influencer)}",')
    out.append(f'            title = "{esc(title)}",')
    out.append(f'            structureNote = "{esc(note)}",')
    out.append("            sections = listOf(")

    for i, (sec, items) in enumerate(sections):
        out.append("                LearnSection(")
        out.append(f'                    id = "{sec_prefix}_{i + 1:02d}",')
        out.append(f'                    title = "{esc(sec)}",')
        out.append(
            f'                    summary = "Practice set imported from internet source for {esc(sec)}.",'
        )
        out.append("                    items = listOf(")
        for j, item in enumerate(items):
            entry = emit_item(item)
            if j != len(items) - 1:
                entry += ","
            out.append(entry)
        out.append("                    )")
        end = "," if i != len(sections) - 1 else ""
        out.append(f"                ){end}")

    out.append("            )")
    out.append("        )")
    return "\n".join(out)


striver_sections = parse_tree_sheet(
    "https://api.github.com/repos/Codensity30/Strivers-A2Z-DSA-Sheet/git/trees/main?recursive=1",
    "st",
)

love_sections = parse_tree_sheet(
    "https://api.github.com/repos/viren-sureja/Love-Babbar-450/git/trees/master?recursive=1",
    "lb",
)

neet_sections = parse_neetcode_markdown(
    "https://raw.githubusercontent.com/envico801/Neetcode-150-and-Blind-75/main/all-practice-questions.md"
)

content = []
content.append("package com.algoviz.plus.ui.learn.data")
content.append("")
content.append("import com.algoviz.plus.ui.learn.model.LearnItem")
content.append("import com.algoviz.plus.ui.learn.model.LearnSection")
content.append("import com.algoviz.plus.ui.learn.model.LearnSheet")
content.append("import com.algoviz.plus.ui.learn.model.LearnTopicTag")
content.append("")
content.append("object CheatSheetCatalog {")
content.append("")
content.append("    val sheets: List<LearnSheet> = listOf(")
content.append(
    emit_sheet(
        "love_babbar",
        "Love Babbar",
        "DSA 450 Sheet",
        "Question list imported from public internet repository structure for Love Babbar 450.",
        love_sections,
        "lb",
    )
    + ","
)
content.append(
    emit_sheet(
        "striver_a2z",
        "Striver (Take U Forward)",
        "A2Z DSA Sheet",
        "Question list imported from public internet repository structure for Striver A2Z.",
        striver_sections,
        "st",
    )
    + ","
)
content.append(
    emit_sheet(
        "neetcode_150",
        "NeetCode",
        "NeetCode Roadmap / 150",
        "Question list imported from public internet roadmap markdown source.",
        neet_sections,
        "nt",
    )
)
content.append("    )")
content.append("")
content.append("    fun findSheet(sheetId: String): LearnSheet? = sheets.firstOrNull { it.id == sheetId }")
content.append("}")

OUT.write_text("\n".join(content), encoding="utf-8")
print("Generated", OUT)
print("Love sections/items:", len(love_sections), sum(len(i) for _, i in love_sections))
print("Striver sections/items:", len(striver_sections), sum(len(i) for _, i in striver_sections))
print("Neet sections/items:", len(neet_sections), sum(len(i) for _, i in neet_sections))
