from math import isfinite


SEGMENT_RULES = (
    (
        "🧭 Academic + attendance support",
        lambda scores: _below(scores, "academic", 60)
        and _below(scores, "attendance", 65),
    ),
    (
        "🎓 Strong academics, placement support",
        lambda scores: _at_least(scores, "academic", 75)
        and _below(scores, "placement", 40),
    ),
    (
        "📚 Good attendance, academic support",
        lambda scores: _at_least(scores, "attendance", 75)
        and _below(scores, "academic", 60),
    ),
    (
        "💼 Placement-ready, academic support",
        lambda scores: _at_least(scores, "placement", 70)
        and _below(scores, "academic", 60),
    ),
    (
        "🔎 Low engagement / LMS activity",
        lambda scores: _below(scores, "engagement", 40)
        or _below(scores, "lms", 50),
    ),
    (
        "🌟 Consistently strong indicators",
        lambda scores: all(
            _at_least(scores, key, 75)
            for key in ("academic", "attendance", "placement")
        ),
    ),
)

SCORE_COLUMNS = {
    "academic": ("academicScore",),
    "attendance": ("attendanceScore", "attendancePct", "subjectAttendanceAvg"),
    "lms": ("lmsScore",),
    "engagement": ("engagementScore",),
    "placement": ("placementReadinessScore", "placementMockScore"),
    "skills": ("skillsScore",),
    "feedback": ("feedbackScore",),
}


def _number(value):
    if value is None or isinstance(value, bool):
        return None
    try:
        parsed = float(value)
    except (TypeError, ValueError):
        return None
    return parsed if isfinite(parsed) else None


def component_scores(student):
    scores = {}
    for component, columns in SCORE_COLUMNS.items():
        for column in columns:
            score = _number(student.get(column))
            if score is not None:
                if component == "academic" and column == "gpa":
                    score *= 10
                scores[component] = score
                break

    if "academic" not in scores:
        academic_inputs = [
            _number(student.get(column))
            for column in ("internalAssessmentAvg", "subjectPerformanceAvg")
        ]
        gpa = _number(student.get("gpa"))
        backlogs = _number(student.get("backlogs"))
        academic_inputs.extend([
            gpa * 10 if gpa is not None else None,
            max(0.0, 100 - backlogs * 20) if backlogs is not None else None
        ])
        if any(value is not None for value in academic_inputs):
            scores["academic"] = sum(
                value for value in academic_inputs if value is not None
            ) / sum(value is not None for value in academic_inputs)

    if "attendance" not in scores:
        attendance_inputs = [
            _number(student.get(column))
            for column in ("attendancePct", "subjectAttendanceAvg")
        ]
        if any(value is not None for value in attendance_inputs):
            scores["attendance"] = sum(
                value for value in attendance_inputs if value is not None
            ) / sum(value is not None for value in attendance_inputs)

    if "lms" not in scores:
        login_frequency = _number(student.get("lmsLoginFrequency"))
        activity_hours = _number(student.get("lmsActivityHrs"))
        lms_inputs = [
            min(login_frequency / 10, 1) * 100 if login_frequency is not None else None,
            min(activity_hours / 20, 1) * 100 if activity_hours is not None else None,
            _number(student.get("assignmentCompletionPct"))
        ]
        if any(value is not None for value in lms_inputs):
            scores["lms"] = sum(
                value for value in lms_inputs if value is not None
            ) / sum(value is not None for value in lms_inputs)

    if "engagement" not in scores:
        engagement_inputs = [
            min(count / full_score, 1) * 100
            for field, full_score in (
                ("eventsParticipated", 10),
                ("clubsParticipated", 3),
                ("hackathonsParticipated", 3),
                ("certifications", 5),
            )
            if (count := _number(student.get(field))) is not None
        ]
        if engagement_inputs:
            scores["engagement"] = sum(engagement_inputs) / len(engagement_inputs)

    if "placement" not in scores:
        placement_inputs = [
            _number(student.get(field))
            for field in ("aptitudeScore", "codingScore", "mockInterviewScore")
        ]
        if all(value is None for value in placement_inputs):
            placement_inputs.append(_number(student.get("placementMockScore")))
        if any(value is not None for value in placement_inputs):
            scores["placement"] = sum(
                value for value in placement_inputs if value is not None
            ) / sum(value is not None for value in placement_inputs)

    for component, fields in (
        ("skills", ("technicalSkillScore", "softSkillScore")),
        ("feedback", ("studentSatisfactionScore", "facultyFeedbackScore")),
    ):
        if component not in scores:
            values = [_number(student.get(field)) for field in fields]
            if any(value is not None for value in values):
                scores[component] = sum(
                    value for value in values if value is not None
                ) / sum(value is not None for value in values)

    return scores


def _below(scores, component, threshold):
    return component in scores and scores[component] < threshold


def _at_least(scores, component, threshold):
    return component in scores and scores[component] >= threshold


def student_segment(student):
    scores = component_scores(student)
    for label, matches in SEGMENT_RULES:
        if matches(scores):
            return label
    required_components = ("academic", "attendance", "placement")
    if not all(component in scores for component in required_components):
        return "🧩 More data needed"
    return "📈 Balanced / monitor"


def _format_percent(value):
    return f"{value:.0f}%"


def _format_score(value):
    return f"{value:.0f}/100"


def main_drivers(student):
    drivers = []
    scores = component_scores(student)
    attendance = scores.get("attendance")
    if attendance is not None and attendance < 75:
        drivers.append((75 - attendance, f"Low attendance ({_format_percent(attendance)})"))

    academic = scores.get("academic")
    if academic is not None and academic < 60:
        drivers.append(
            (60 - academic, f"Low academic performance ({_format_score(academic)})")
        )
    backlogs = _number(student.get("backlogs"))
    if backlogs is not None and backlogs > 0:
        drivers.append((backlogs * 10, f"{backlogs:.0f} reported backlog(s)"))

    placement = scores.get("placement")
    if placement is not None and placement < 70:
        drivers.append(
            (70 - placement, f"Low placement readiness ({_format_score(placement)})")
        )
    for field, label in (
        ("aptitudeScore", "Aptitude"),
        ("codingScore", "Coding"),
        ("mockInterviewScore", "Mock interviews"),
        ("placementMockScore", "Placement practice"),
    ):
        value = _number(student.get(field))
        if value == 0 and field == "mockInterviewScore":
            drivers.append((40, "No mock interview practice (0/100)"))
        elif value is not None and value < 40:
            drivers.append((40 - value, f"Low {label.lower()} score ({_format_score(value)})"))

    lms = scores.get("lms")
    if lms is not None and lms < 50:
        drivers.append((50 - lms, f"Low LMS activity ({_format_score(lms)})"))
    assignment_completion = _number(student.get("assignmentCompletionPct"))
    if assignment_completion is not None and assignment_completion < 60:
        drivers.append(
            (60 - assignment_completion,
             f"Low assignment completion ({_format_percent(assignment_completion)})")
        )
    login_frequency = _number(student.get("lmsLoginFrequency"))
    if login_frequency == 0:
        drivers.append((35, "No LMS logins recorded"))

    engagement = scores.get("engagement")
    if engagement is not None and engagement < 40:
        drivers.append(
            (40 - engagement, f"Low engagement ({_format_score(engagement)})")
        )

    skills = scores.get("skills")
    if skills is not None and skills < 55:
        drivers.append((55 - skills, f"Skills need support ({_format_score(skills)})"))

    feedback = scores.get("feedback")
    if feedback is not None and feedback < 50:
        drivers.append(
            (50 - feedback, f"Low feedback indicator ({_format_score(feedback)})")
        )

    risk_flags = [
        f"{label} risk: {student.get(field)}"
        for field, label in (
            ("academicRisk", "Academic"),
            ("placementRisk", "Placement"),
            ("riskCategory", "Overall"),
        )
        if student.get(field) in {"High", "Moderate"}
    ]
    drivers.sort(key=lambda item: item[0], reverse=True)
    explanations = [reason for _, reason in drivers[:3]]
    explanations.extend(risk_flags)

    if explanations:
        return " · ".join(explanations)
    if student.get("actionableInsight"):
        return str(student["actionableInsight"])
    return "No low-score driver detected in the available data."
