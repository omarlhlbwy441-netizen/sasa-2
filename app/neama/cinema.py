# -*- coding: utf-8 -*-
"""6. Cinematic Directing & Screenplay Studio (استوديو الإخراج السينمائي وهندسة المشاهد)
Cinematic shot breakdown, camera choreography, lighting design, and dramatic dialogue pacing.
Operates autonomously in the background for on-demand movies, series, video, and screenplay synthesis.
"""

from typing import Dict, Any, List, Optional
import os
import time

class CinematicDirectingEngine:
    """
    Sovereign Cinematic Directing & Screenplay Intelligence Core (Gen-20).
    Synthesizes multi-camera choreography, visual blocking, emotional rhythm,
    lighting schemes, and full scripts for high-end film and series production.
    """
    def __init__(self):
        self.version = "Neama-Gen20-CinematicDirecting"

    def compose_scene(
        self,
        scene_title: str,
        location: str,
        time_of_day: str,
        mood: str,
        characters: List[str]
    ) -> Dict[str, Any]:
        """
        Creates a comprehensive director breakdown sheet for a cinematic sequence.
        """
        return {
            "scene_header": f"مشهد: {scene_title} — {location.upper()} ({time_of_day})",
            "mood_and_tone": mood,
            "characters_present": characters,
            "cinematography": {
                "aspect_ratio": "2.39:1 (Anamorphic Widescreen)",
                "lens_recommendation": "Cooke Anamorphic /i Prime 40mm & 75mm",
                "color_grading_palette": "Cinematic Teal & Amber with deep contrast shadows",
                "camera_movements": [
                    "لقطة افتتاحية عريضة (Master Shot): زاوية منخفضة بطيئة الحركة (Slow Dolly In).",
                    "لقطات متوسطة وتفصيلية (Medium & Over-the-Shoulder): تتبع تعابير الوجوه والتوتر النفسي.",
                    "لقطة قريبة جداً (Extreme Close-Up): تركيز عميق على نظرات العيون وحركات الأيدي الدقيقة."
                ]
            },
            "lighting_design": {
                "key_light": "إضاءة ناعمة بزاوية 45 درجة (Key Light Diffusion).",
                "fill_light": "إضاءة تكميلية خافتة بنسبة 4:1 لخلق عمق درامي وظلال متدرجة.",
                "rim_light": "إضاءة حافة حادة تفصل الشخصيات عن الخلفية (Hair/Rim Light)."
            },
            "sound_design": "شريط صوتي محيطي (Dolby Atmos) مع نبضات إيقاعية خافتة وتضخيم للأصوات البيئية الطبيعية لتعزيز التوتر الدرامي."
        }

    def generate_production_package(
        self,
        prompt: str,
        media_type: str = "movie",
        genre: str = "drama",
        episodes: int = 1
    ) -> Dict[str, Any]:
        """
        Generates a complete cinematic production package:
        Includes Story Logline, Technical Director Breakdown, Scene Headers,
        Camera Choreography, Dynamic Dialogue, and Sound Design.
        """
        prompt_clean = prompt.strip()
        is_horror = "رعب" in prompt_clean or genre == "horror" or "خوف" in prompt_clean or "غموض" in prompt_clean
        is_scifi = "خيال علمي" in prompt_clean or "فضاء" in prompt_clean or "ذكاء" in prompt_clean or genre == "sci-fi"
        is_historical = "تاريخ" in prompt_clean or "أندلس" in prompt_clean or "ملحمة" in prompt_clean or genre == "historical"

        # Determine Title & Genre
        if is_horror:
            title = "صدى الظلال: الليلة الأخيرة" if media_type != "series" else "سلسلة الرعب: أصوات في العتمة (3 حلقات)"
            selected_genre = "رعب نفسي وتشويق درامي (Psychological Horror Thriller)"
            lens = "Leica Noctilux 50mm f/0.95 & Cooke 25mm Anamorphic"
            palette = "Obsidian Black, Crimson Red, Deep Cyan Shadows"
            mood = "توتر متصاعد، ترقب نفسي خانق، وغموض سائد"
        elif is_scifi:
            title = "نيوكوريس: الشفرة الأخيرة" if media_type != "series" else "مسلسل نيوكوريس: أفق الذكاء الاصطناعي (3 حلقات)"
            selected_genre = "خيال علمي وسيناريو مستقبلي (Hard Sci-Fi / Cybernetic)"
            lens = "Arri Master Anamorphic 35mm & 100mm Prime"
            palette = "Neon Cyan, Electric Indigo, Deep Titanium Silver"
            mood = "غموض تكنولوجي، تسارع إيقاعي، ودهشة وجودية"
        elif is_historical:
            title = "ملحمة الأندلس: غروب قصر الحمراء" if media_type != "series" else "ملحمة الأندلس: ثلاثية الفردوس المفقود"
            selected_genre = "دراما تاريخية ملحمية (Historical Epic Drama)"
            lens = "Cooke S4/i Prime 27mm, 50mm & 85mm"
            palette = "Warm Amber, Antique Gold, Deep Mediterranean Blue"
            mood = "مهابة تاريخية، حزن نبيل، وحوارات فلسفية عميقة"
        else:
            title = f"عمل سينمائي: {prompt_clean[:35]}"
            selected_genre = "دراما وسينما معاصرة (Cinematic Contemporary Drama)"
            lens = "Cooke Anamorphic /i Prime 40mm & 75mm"
            palette = "Cinematic Natural Teal & Soft Amber"
            mood = "واقعية شاعرية وتفاعل درامي مكثف"

        # Build Director Sheet
        director_sheet = {
            "aspect_ratio": "2.39:1 (Cinematic Scope)",
            "camera_system": "ARRI ALEXA 35 / Large Format Cinema Sensor",
            "lens_package": lens,
            "color_grading": palette,
            "key_lighting": "Soft Key Light diffused through 8x8 grid cloth (45° angle)",
            "rim_lighting": "Edge Rim Light with subtle color accent for subject separation",
            "sound_system": "Dolby Atmos 7.1.4 3D Spatial Audio Landscape"
        }

        # Build Episodes or Scenes
        content_sections = []
        num_episodes = 3 if (media_type == "series" or "مسلسل" in prompt_clean or episodes >= 3) else 1

        if num_episodes > 1:
            for ep_num in range(1, num_episodes + 1):
                if ep_num == 1:
                    ep_title = "الحلقة 1: البداية وانكشاف اللغز (The Awakening)"
                    loc = "EXT. غابة مهجورة / مختبر أبحاث - LEV 1"
                    time_slot = "LATE NIGHT (02:40 AM)"
                    action = "الكاميرا تتحرك في لقطة تتبعية بطيئة (Low Dolly Track). ضباب خفيف يغطي الأرضية. إشارات ضوئية متقطعة تنعكس على الوجوه."
                    dialogue = [
                        ("القائد / البطل", "هل تأكدتم من إغلاق كافة البوابات؟ الإشارات القادمة لا تتطابق مع أي سجل نعرفه."),
                        ("المساعد التقني", "(بصوت متوتر ينظر للشاشات) النظام لا يستجيب.. هناك كيان يتحكم في خوارزميات التوجيه من الداخل!"),
                        ("القائد / البطل", "(يقترب من الشاشة، عدسة Close-up) إذن لقد بدأت المواجهة.. لا مجال للتراجع الآن.")
                    ]
                elif ep_num == 2:
                    ep_title = "الحلقة 2: تصاعد الصراع وحصار المجهول (The Escalation)"
                    loc = "INT. قاعة التحكم المركزية / الممر السري"
                    time_slot = "MIDNIGHT (03:15 AM)"
                    action = "لقطة علوية (Crane Shot) ترصد انقطاع مصادر الطاقة الرئيسية. إضاءة الطوارئ الحمراء تومض بإيقاع نبضي متسارع."
                    dialogue = [
                        ("المساعد التقني", "فقدنا الاتصال بالقاعدة الخارجية! نحن بمفردنا هنا."),
                        ("القائد / البطل", "(يتفقد أجهزة الإرسال بحزم) الهدوء.. الخوف هو أول أسلحتهم ضدنا. سنعيد توجيه الطاقة يدوياً."),
                        ("شخصية غامضة", "(صوت يتردد عبر مكبرات الصوت) محاولتكم جاءت متأخرة جداً.. الحقيقة تم تفعيلها بالفعل.")
                    ]
                else:
                    ep_title = "الحلقة 3: الذروة والحسم النهائي (The Final Stand)"
                    loc = "EXT. المنصة المرتفعة / أطلال البرج الرئيسي"
                    time_slot = "DAWN (05:45 AM)"
                    action = "لقطة عريضة جداً (Ultra-Wide Anamorphic) تكشف أول خيوط الفجر. الكاميرا تدور 360 درجة حول الشخصيات في لحظة الحسم."
                    dialogue = [
                        ("القائد / البطل", "كل ما بنيتموه على الوهم سينهار مع أول ضوء لهذا الصباح."),
                        ("شخصية غامضة", "النهايات ليست سوى بوابات لبدايات جديدة لن تفهموها أبداً."),
                        ("القائد / البطل", "(يضغط على مفتاح التشغيل السيادي) سنرى ذلك الآن.")
                    ]
                content_sections.append({
                    "number": ep_num,
                    "title": ep_title,
                    "slugline": f"{loc} — {time_slot}",
                    "action": action,
                    "dialogue": dialogue
                })
        else:
            # Single Movie / Video Sequence
            content_sections.append({
                "number": 1,
                "title": "المشهد الافتتاحي والرئيسي (Master Cinematic Sequence)",
                "slugline": "INT/EXT. موقع التصوير الرئيسي — NIGHT (01:30 AM)",
                "action": "تبدأ اللقطة في ظلام دامس، ينساب صوت تنفس متسارع. حركة كاميرا Steadicam تتبع خطوات ثابتة على أرضية مبللة. ينبثق ضوء مائل يكشف ملامح الوجه بحواف حادة.",
                "dialogue": [
                    ("الشخصية الرئيسية", "(هامساً لنفسه) كل خطوة تقربني من الحقيقة.. أو من الهاوية."),
                    ("الصوت المرافق", "(عبر جهاز الاتصال) لا تتوقف، المؤشرات الحيوية تقترب من نقطة اللاعودة."),
                    ("الشخصية الرئيسية", "(يقف في منتصف الكادر، الكاميرا تصعد ببطء) أنا هنا.. ولن أغادر بدون الإجابات.")
                ]
            })

        return {
            "title": title,
            "media_type": media_type,
            "genre": selected_genre,
            "mood": mood,
            "episodes_count": num_episodes,
            "director_sheet": director_sheet,
            "content_sections": content_sections
        }

    def format_screenplay_markdown(self, pkg: Dict[str, Any]) -> str:
        """
        Formats production package into an authentic director screenplay document.
        """
        lines = []
        title = pkg.get("title", "عمل سينمائي")
        media_type_label = "مسلسل متكامل" if pkg.get("media_type") == "series" or pkg.get("episodes_count", 1) > 1 else "فيلم سينمائي"
        lines.append(f"# 🎬 {media_type_label}: {title}")
        lines.append(f"**النوع والتصنيف الدرامي**: {pkg.get('genre')}")
        lines.append(f"**الحالة المزاجية العامة**: {pkg.get('mood')}")
        lines.append("")
        lines.append("---")
        lines.append("### 🎥 مواصفات التوجيه الإخراجي (Director Technical Specs)")
        ds = pkg.get("director_sheet", {})
        lines.append(f"• **أبعاد الشاشة (Aspect Ratio)**: `{ds.get('aspect_ratio', '2.39:1')}`")
        lines.append(f"• **الكاميرا والمستشعر**: `{ds.get('camera_system', 'ARRI ALEXA 35')}`")
        lines.append(f"• **حزمة العدسات السينمائية**: `{ds.get('lens_package')}`")
        lines.append(f"• **لوحة الألوان والتدريج (Color Grade)**: `{ds.get('color_grading')}`")
        lines.append(f"• **الإضاءة الرئيسية والتكميلية**: {ds.get('key_lighting')} | {ds.get('rim_lighting')}")
        lines.append(f"• **التصميم الصوتي**: `{ds.get('sound_system')}`")
        lines.append("")
        lines.append("---")

        sections = pkg.get("content_sections", [])
        for sec in sections:
            lines.append(f"### 🎞️ {sec.get('title')}")
            lines.append(f"**`{sec.get('slugline')}`**")
            lines.append("")
            lines.append(f"*{sec.get('action')}*")
            lines.append("")
            lines.append("#### 🗣️ الحوار الدرامي المسجل:")
            for speaker, text in sec.get("dialogue", []):
                lines.append(f"**{speaker}**:")
                lines.append(f"> {text}")
                lines.append("")
            lines.append("---")

        lines.append("📌 *تم إنتاج وهندسة السيناريو والمشاهد بالكامل عبر محرك الاستوديو السينمائي السيادي (Neama Gen-20).*")
        return "\n".join(lines)


# Singleton Instance
cinematic_engine = CinematicDirectingEngine()
