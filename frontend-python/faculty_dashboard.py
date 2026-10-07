import streamlit as st
import pandas as pd
import plotly.express as px
import plotly.graph_objects as go
import plotly.io as pio
import requests
from math import isfinite
from services.student_service import (
    add_course_grade,
    add_student,
    delete_course_grade,
    get_all_students,
    get_course_grades
)
from services.faculty_portal_service import (
    get_anonymous_staff_feedback,
    get_my_student_feedback,
    get_weekly_student_activity,
    submit_student_feedback
)
from faculty_insights import component_scores, main_drivers, student_segment
from theme import BURGUNDY, CHART_COLORS, ROSE

pio.templates["smart_campus"] = go.layout.Template(
    layout=go.Layout(
        paper_bgcolor="#FFF9FB",
        plot_bgcolor="#FFF9FB",
        font={"family": "Arial, sans-serif", "color": BURGUNDY},
        title={"font": {"color": BURGUNDY, "size": 17}},
        xaxis={
            "gridcolor": "#F1DDE4",
            "linecolor": "#D9A6B5",
            "tickfont": {"color": "#754357"}
        },
        yaxis={
            "gridcolor": "#F1DDE4",
            "linecolor": "#D9A6B5",
            "tickfont": {"color": "#754357"}
        },
        colorway=CHART_COLORS,
        margin={"l": 24, "r": 20, "t": 48, "b": 28}
    )
)
px.defaults.template = "smart_campus"
px.defaults.color_discrete_sequence = CHART_COLORS

DEPARTMENTS = [
    "CSE", "ECE", "EEE", "MECH", "CIVIL", "IT", "AI & Data Science",
    "Artificial Intelligence & Machine Learning", "Cyber Security",
    "Biotechnology", "Biomedical Engineering", "Chemical Engineering",
    "Aeronautical Engineering", "Automobile Engineering",
    "Agricultural Engineering", "Mechatronics", "Food Technology",
    "Textile Technology"
]
REQUIRED_STUDENT_COLUMNS = ["studentId", "name", "department", "semester"]
NUMERIC_FIELDS = {
    "semester": (1, 8, True),
    "gpa": (0, 10, False),
    "internalAssessmentAvg": (0, 100, False),
    "backlogs": (0, 100, True),
    "subjectPerformanceAvg": (0, 100, False),
    "attendancePct": (0, 100, False),
    "subjectAttendanceAvg": (0, 100, False),
    "lmsActivityHrs": (0, 50, True),
    "lmsLoginFrequency": (0, 100, True),
    "assignmentCompletionPct": (0, 100, False),
    "eventsParticipated": (0, 100, True),
    "clubsParticipated": (0, 100, True),
    "hackathonsParticipated": (0, 100, True),
    "certifications": (0, 100, True),
    "aptitudeScore": (0, 100, False),
    "codingScore": (0, 100, False),
    "mockInterviewScore": (0, 100, False),
    "placementMockScore": (0, 100, True),
    "technicalSkillScore": (0, 100, False),
    "softSkillScore": (0, 100, False),
    "studentSatisfactionScore": (0, 100, False),
    "facultyFeedbackScore": (0, 100, False)
}
STUDENT_CSV_COLUMNS = [
    *REQUIRED_STUDENT_COLUMNS,
    *[field for field in NUMERIC_FIELDS if field != "semester"]
]
COMMON_COURSES = {
    1: [
        ("GE23101", "தமிழர் மரபு / Heritage of Tamil"),
        ("MA23101", "Matrices and Calculus"),
        ("CH23201", "Applied Chemistry"),
        ("EE23101", "Basic Electrical and Electronics Engineering"),
        ("CS23102", "Programming in C"),
        ("EN23101", "Communication Skills for Engineers I"),
        ("CH23104", "Chemistry Laboratory"),
        ("GE23101", "Electrical and Electronics Engineering Practice Laboratory"),
        ("CS23104", "Programming in C Laboratory")
    ],
    2: [
        ("GE23201", "தமிழரும் தொழில்நுட்பமும் / Tamils and Technology"),
        ("MA23202", "Differential Equations and Numerical Techniques"),
        ("PH23201", "Physics for Information Science"),
        ("ME23201", "Engineering Graphics"),
        ("CS23201", "Problem Solving and Python Programming"),
        ("EN23201", "Communication Skills for Engineers II"),
        ("PH23204", "Physics Laboratory for Information Science"),
        ("GE23203", "Civil and Mechanical Engineering Practice Laboratory"),
        ("CS23202", "Problem Solving and Python Programming Laboratory")
    ],
}
IT_COURSES = {
    3: [
        ("MA23301", "Discrete Mathematics"),
        ("IT23301", "Computer Architecture"),
        ("IT23302", "Data Structures"),
        ("IT23303", "Object Oriented Programming"),
        ("MC23301", "Environmental Science and Engineering"),
        ("EC23306", "Digital Principles and System Design"),
        ("IT23306", "Data Structures Laboratory"),
        ("IT23307", "Object Oriented Programming Laboratory"),
        ("EN23301", "Professional Development I")
    ],
    4: [
        ("MA23401", "Probability and Statistics"),
        ("IT23401", "Database Management Systems"),
        ("IT23402", "Operating Systems"),
        ("IT23403", "Design and Analysis of Algorithms"),
        ("MC23401", "Human Values Gender Equality"),
        ("IT23404", "Computer Networks"),
        ("IT23405", "Database Management Systems Laboratory"),
        ("IT23406", "Operating Systems Laboratory"),
        ("EN23401", "Professional Development II")
    ],
    5: [
        ("IT23501", "Theory of Computation"),
        ("IT23502", "Artificial Intelligence"),
        ("IT23503", "Foundations of Data Science"),
        ("IT23504", "Full Stack Development"),
        ("IT23505", "Software Engineering"),
        ("IT2315*", "Professional Elective - I"),
        ("IT23505", "Artificial Intelligence Laboratory"),
        ("IT23506", "Data Science Laboratory"),
        ("IT23508", "Industrial Training"),
        ("EN23501", "Professional Development III")
    ],
    6: [
        ("BA23151", "Entrepreneurship Development"),
        ("IT23601", "Cryptography and Network Security"),
        ("IT23602", "Internet of Things"),
        ("IT23603", "Machine Learning"),
        ("IT2325*", "Professional Elective - II"),
        ("IT2390*", "Open Elective - I"),
        ("IT23604", "Internet of Things Laboratory"),
        ("IT23605", "Machine Learning Laboratory"),
        ("IT23607", "Design Thinking")
    ],
    7: [
        ("IT23701", "Big Data and Analytics"),
        ("IT23702", "Block Chain Technology"),
        ("IT23703", "Cloud Computing"),
        ("IT2335*", "Professional Elective - III"),
        ("IT2345*", "Professional Elective - IV"),
        ("IT2390*", "Open Elective - II"),
        ("IT23704", "Cloud Computing Laboratory"),
        ("IT23705", "Mini Project")
    ],
    8: [
        ("IT2355*", "Professional Elective - V"),
        ("IT2365*", "Professional Elective - VI"),
        ("IT23801", "Project Work")
    ]
}
SUGGESTED_DEPARTMENT_COURSES = {
    "CSE": {
        3: ["Discrete Mathematics", "Data Structures", "Object-Oriented Programming"],
        4: ["Database Management Systems", "Operating Systems", "Design and Analysis of Algorithms"],
        5: ["Computer Networks", "Software Engineering", "Theory of Computation"],
        6: ["Artificial Intelligence", "Machine Learning", "Compiler Design"],
        7: ["Distributed Systems", "Cloud Computing", "Information Security"],
        8: ["Advanced Computing Elective", "Major Project", "Internship"]
    },
    "ECE": {
        3: ["Electronic Devices and Circuits", "Digital System Design", "Signals and Systems"],
        4: ["Analog Circuits", "Electromagnetic Fields", "Microprocessors and Microcontrollers"],
        5: ["Communication Systems", "Digital Signal Processing", "Control Systems"],
        6: ["VLSI Design", "Wireless Communication", "Embedded Systems"],
        7: ["Optical Communication", "Antenna and Wave Propagation", "Internet of Things"],
        8: ["Advanced Electronics Elective", "Major Project", "Internship"]
    },
    "EEE": {
        3: ["Electrical Circuit Analysis", "Electromagnetic Fields", "Electrical Machines I"],
        4: ["Electrical Machines II", "Analog and Digital Electronics", "Measurements and Instrumentation"],
        5: ["Power Systems I", "Power Electronics", "Control Systems"],
        6: ["Power Systems II", "Electrical Drives", "Microprocessors"],
        7: ["Renewable Energy Systems", "Smart Grid Technologies", "High Voltage Engineering"],
        8: ["Advanced Electrical Engineering Elective", "Major Project", "Internship"]
    },
    "MECH": {
        3: ["Engineering Mechanics", "Manufacturing Technology I", "Thermodynamics"],
        4: ["Fluid Mechanics", "Strength of Materials", "Manufacturing Technology II"],
        5: ["Heat and Mass Transfer", "Design of Machine Elements", "Applied Thermodynamics"],
        6: ["Dynamics of Machines", "Refrigeration and Air Conditioning", "Finite Element Methods"],
        7: ["Computer Integrated Manufacturing", "Automobile Engineering", "Robotics"],
        8: ["Advanced Mechanical Engineering Elective", "Major Project", "Internship"]
    },
    "CIVIL": {
        3: ["Surveying", "Strength of Materials", "Building Materials and Construction"],
        4: ["Structural Analysis I", "Fluid Mechanics", "Geotechnical Engineering I"],
        5: ["Design of Reinforced Concrete Structures", "Transportation Engineering", "Hydrology"],
        6: ["Design of Steel Structures", "Geotechnical Engineering II", "Environmental Engineering"],
        7: ["Construction Project Management", "Advanced Structural Engineering", "Water Resources Engineering"],
        8: ["Advanced Civil Engineering Elective", "Major Project", "Internship"]
    },
    "AI & Data Science": {
        3: ["Data Structures", "Statistics for Data Science", "Python for Data Analytics"],
        4: ["Database Systems", "Data Visualization", "Foundations of Machine Learning"],
        5: ["Big Data Technologies", "Deep Learning", "Data Mining"],
        6: ["Natural Language Processing", "Time Series Analytics", "Cloud Data Engineering"],
        7: ["Responsible AI", "Business Intelligence", "Applied Data Science"],
        8: ["Advanced Data Science Elective", "Major Project", "Internship"]
    },
    "Artificial Intelligence & Machine Learning": {
        3: ["Data Structures", "Linear Algebra for AI", "Probability for Machine Learning"],
        4: ["Machine Learning", "Knowledge Representation", "Database Systems"],
        5: ["Deep Learning", "Computer Vision", "Natural Language Processing"],
        6: ["Reinforcement Learning", "Big Data Analytics", "MLOps"],
        7: ["Generative AI", "AI Ethics and Governance", "Applied Robotics"],
        8: ["Advanced AI Elective", "Major Project", "Internship"]
    },
    "Cyber Security": {
        3: ["Data Structures", "Computer Networks", "Foundations of Cyber Security"],
        4: ["Operating Systems", "Cryptography", "Secure Programming"],
        5: ["Network Security", "Ethical Hacking", "Web Application Security"],
        6: ["Digital Forensics", "Cloud Security", "Malware Analysis"],
        7: ["Security Operations and Incident Response", "Penetration Testing", "Cyber Law"],
        8: ["Advanced Cyber Security Elective", "Major Project", "Internship"]
    },
    "Biotechnology": {
        3: ["Cell Biology", "Biochemistry", "Microbiology"],
        4: ["Molecular Biology", "Genetics", "Bioprocess Engineering"],
        5: ["Immunology", "Genetic Engineering", "Enzyme Technology"],
        6: ["Downstream Processing", "Plant Biotechnology", "Industrial Biotechnology"],
        7: ["Genomics and Proteomics", "Biopharmaceutical Technology", "Bioinformatics"],
        8: ["Advanced Biotechnology Elective", "Major Project", "Internship"]
    },
    "Biomedical Engineering": {
        3: ["Human Anatomy and Physiology", "Biomedical Instrumentation", "Medical Sensors"],
        4: ["Biomaterials", "Medical Imaging Systems", "Biomedical Signal Processing"],
        5: ["Diagnostic and Therapeutic Equipment", "Rehabilitation Engineering", "Biostatistics"],
        6: ["Clinical Engineering", "Medical Image Analysis", "Biomechanics"],
        7: ["Wearable Health Technologies", "Healthcare Information Systems", "Tissue Engineering"],
        8: ["Advanced Biomedical Engineering Elective", "Major Project", "Internship"]
    },
    "Chemical Engineering": {
        3: ["Chemical Process Calculations", "Fluid Flow Operations", "Chemical Engineering Thermodynamics"],
        4: ["Heat Transfer", "Mass Transfer I", "Mechanical Operations"],
        5: ["Chemical Reaction Engineering", "Mass Transfer II", "Process Instrumentation"],
        6: ["Process Dynamics and Control", "Chemical Plant Design", "Petroleum Refining"],
        7: ["Process Safety", "Separation Processes", "Biochemical Engineering"],
        8: ["Advanced Chemical Engineering Elective", "Major Project", "Internship"]
    },
    "Aeronautical Engineering": {
        3: ["Aerodynamics I", "Aircraft Materials", "Aircraft Systems"],
        4: ["Aircraft Structures I", "Propulsion Systems", "Flight Mechanics"],
        5: ["Aerodynamics II", "Aircraft Structures II", "Gas Turbine Engineering"],
        6: ["Aircraft Design", "Avionics", "Computational Fluid Dynamics"],
        7: ["Space Vehicle Technology", "Aircraft Stability and Control", "Aviation Safety"],
        8: ["Advanced Aeronautical Engineering Elective", "Major Project", "Internship"]
    },
    "Automobile Engineering": {
        3: ["Automotive Engines", "Automotive Chassis", "Automotive Electrical Systems"],
        4: ["Vehicle Dynamics", "Automotive Transmission", "Manufacturing Processes"],
        5: ["Automotive Emission Control", "Vehicle Body Engineering", "Automotive Electronics"],
        6: ["Hybrid and Electric Vehicles", "Automotive Safety", "Automotive Control Systems"],
        7: ["Vehicle Diagnostics", "Autonomous Vehicles", "Automotive Product Design"],
        8: ["Advanced Automobile Engineering Elective", "Major Project", "Internship"]
    },
    "Agricultural Engineering": {
        3: ["Soil Science and Engineering", "Farm Machinery", "Surveying and Leveling"],
        4: ["Soil and Water Conservation", "Irrigation Engineering", "Agricultural Structures"],
        5: ["Crop Processing Engineering", "Tractors and Power Units", "Watershed Management"],
        6: ["Agricultural Process Engineering", "Remote Sensing in Agriculture", "Food and Bioprocess Engineering"],
        7: ["Precision Agriculture", "Renewable Energy in Agriculture", "Post-Harvest Technology"],
        8: ["Advanced Agricultural Engineering Elective", "Major Project", "Internship"]
    },
    "Mechatronics": {
        3: ["Sensors and Transducers", "Electrical Machines", "Engineering Mechanics"],
        4: ["Microcontrollers", "Manufacturing Technology", "Control Systems"],
        5: ["Industrial Robotics", "Hydraulics and Pneumatics", "Embedded Systems"],
        6: ["Machine Vision", "PLC and SCADA", "Mechatronic System Design"],
        7: ["Autonomous Systems", "Advanced Robotics", "Industrial Automation"],
        8: ["Advanced Mechatronics Elective", "Major Project", "Internship"]
    },
    "Food Technology": {
        3: ["Food Chemistry", "Food Microbiology", "Food Process Engineering"],
        4: ["Food Preservation", "Food Biochemistry", "Food Engineering Operations"],
        5: ["Dairy Technology", "Food Packaging", "Food Quality Assurance"],
        6: ["Fruit and Vegetable Processing", "Cereal Technology", "Food Safety Management"],
        7: ["Meat and Fish Processing", "Food Product Development", "Food Plant Design"],
        8: ["Advanced Food Technology Elective", "Major Project", "Internship"]
    },
    "Textile Technology": {
        3: ["Textile Fibre Science", "Yarn Manufacturing I", "Textile Testing"],
        4: ["Yarn Manufacturing II", "Fabric Structure", "Textile Chemical Processing"],
        5: ["Weaving Technology", "Knitting Technology", "Textile Machinery"],
        6: ["Garment Manufacturing", "Technical Textiles", "Textile Quality Management"],
        7: ["Textile Product Design", "Sustainable Textiles", "Apparel Production Management"],
        8: ["Advanced Textile Technology Elective", "Major Project", "Internship"]
    }
}
OTHER_COURSE = (None, "Other / not listed")
GRADE_OPTIONS = ["O", "A+", "A", "B+", "B", "C", "S", "U", "RA", "SA", "WH", "Other / enter manually"]


def semester_course_options(department, semester):
    if semester in COMMON_COURSES:
        return COMMON_COURSES[semester]
    if department == "IT":
        return IT_COURSES[semester]
    return [(None, name) for name in SUGGESTED_DEPARTMENT_COURSES[department][semester]]


def optional_number_input(column, label, field):
    minimum, maximum, _ = NUMERIC_FIELDS[field]
    return column.text_input(
        label,
        placeholder=f"Enter {minimum} to {maximum}",
        key=f"student_{field}",
        help=f"Optional. Enter a value from {minimum} to {maximum}; leave blank if unavailable."
    )


def optional_feedback_input(column, label, field):
    return column.text_input(
        label,
        key=f"student_{field}",
        placeholder="Enter a score from 0 to 100",
        help="Optional. Enter a number from 0 to 100, such as 85."
    )


def parse_feedback_score(value, label):
    if not value.strip():
        return None
    try:
        score = float(value)
    except ValueError as exc:
        raise ValueError(f"{label} must be a number between 0 and 100.") from exc
    if not isfinite(score) or not 0 <= score <= 100:
        raise ValueError(f"{label} must be between 0 and 100.")
    return score


def parse_optional_number(value, field, label):
    if not value.strip():
        return None

    minimum, maximum, must_be_integer = NUMERIC_FIELDS[field]
    try:
        numeric_value = float(value)
    except ValueError as exc:
        raise ValueError(f"{label} must be a number between {minimum} and {maximum}.") from exc

    if not isfinite(numeric_value) or not minimum <= numeric_value <= maximum:
        raise ValueError(f"{label} must be between {minimum} and {maximum}.")
    if must_be_integer and not numeric_value.is_integer():
        raise ValueError(f"{label} must be a whole number.")

    return int(numeric_value) if must_be_integer else numeric_value


def filter_students(student_df, query):
    query = query.strip()
    if not query or student_df.empty:
        return student_df

    searchable_columns = [
        column for column in ("studentId", "name", "department")
        if column in student_df.columns
    ]
    if not searchable_columns:
        return student_df.iloc[0:0]

    search_text = (
        student_df[searchable_columns]
        .fillna("")
        .astype(str)
        .agg(" ".join, axis=1)
    )
    return student_df.loc[
        search_text.str.contains(query, case=False, regex=False, na=False)
    ]


st.sidebar.title("📌 Smart Campus Navigation")
page = st.sidebar.radio(
    "Go to",
    [
        "📊 Executive Dashboard",
        "🧩 Student Segments & Explainable Insights",
        "📈 Weekly Student Activity",
        "💬 Feedback",
        "👥 Saved Student Details",
        "📚 Individual Course Grades",
        "➕ Add Student Profile"
    ]
)

# Fetch Data from Spring Boot REST API
students = get_all_students()
all_students_df = pd.DataFrame(students) if students else pd.DataFrame()
student_search = ""
if page != "➕ Add Student Profile":
    student_search = st.sidebar.text_input(
        "Search students",
        placeholder="Name, register no. or department",
        key="faculty_student_search"
    )
df = filter_students(all_students_df, student_search)
if student_search.strip():
    st.sidebar.caption(f"{len(df)} of {len(all_students_df)} students match")

if page == "📊 Executive Dashboard":
    st.title("🎓 Student Success Analytics")
    
    if df.empty:
        if student_search.strip() and not all_students_df.empty:
            st.info(f"No students match “{student_search.strip()}”.")
        else:
            st.warning("No student records found or Spring Boot API is offline.")
    else:
        academic_risk = df.get("academicRisk", pd.Series(index=df.index, dtype="object"))
        placement_risk = df.get("placementRisk", pd.Series(index=df.index, dtype="object"))
        academic_at_risk = academic_risk.isin(["High", "Moderate"]).sum()
        placement_at_risk = placement_risk.isin(["High", "Moderate"]).sum()
        average_score = pd.to_numeric(
            df["successScore"] if "successScore" in df else pd.Series(index=df.index, dtype="float64"),
            errors="coerce"
        ).mean()
        average_coverage = pd.to_numeric(
            df["dataCoveragePct"] if "dataCoveragePct" in df else pd.Series(index=df.index, dtype="float64"),
            errors="coerce"
        ).mean()

        col1, col2, col3 = st.columns(3)
        col1.metric("Total Students", len(df))
        col2.metric("Academic Risk", int(academic_at_risk))
        col3.metric("Placement Risk", int(placement_at_risk))
        col4, col5 = st.columns(2)
        col4.metric(
            "Avg Success Score",
            f"{average_score:.1f} / 100" if pd.notna(average_score) else "N/A"
        )
        col5.metric(
            "Data Coverage",
            f"{average_coverage:.0f}%" if pd.notna(average_coverage) else "N/A"
        )

        st.markdown("---")
        st.caption(
            "Risk flags and weighted scores are transparent decision-support indicators; "
            "they are not a validated machine-learning prediction."
        )

        # Interactive Charts
        c1, c2 = st.columns(2)

        with c1:
            st.subheader("Risk Distribution")
            if "riskCategory" in df and df["riskCategory"].notna().any():
                fig_pie = px.pie(
                    df,
                    names="riskCategory",
                    color="riskCategory",
                    color_discrete_map={
                        "High": BURGUNDY,
                        "Moderate": ROSE,
                        "Low": "#8A6876"
                    },
                    hole=0.4
                )
                st.plotly_chart(fig_pie, use_container_width=True, height=360)
            else:
                st.info("Risk score is unavailable until student indicators are entered.")

        with c2:
            st.subheader("At-Risk Students by Area")
            flags_df = pd.DataFrame({
                "Risk type": ["Academic", "Placement"],
                "At-risk students": [int(academic_at_risk), int(placement_at_risk)]
            })
            fig_flags = px.bar(
                flags_df,
                x="Risk type",
                y="At-risk students",
                color="Risk type",
                text="At-risk students",
                color_discrete_sequence=[BURGUNDY, ROSE]
            )
            st.plotly_chart(fig_flags, use_container_width=True, height=360)

        st.subheader("Student Success Indicator Trends")
        component_columns = {
            "Academic": "academicScore",
            "Attendance": "attendanceScore",
            "LMS": "lmsScore",
            "Engagement": "engagementScore",
            "Placement": "placementReadinessScore",
            "Skills": "skillsScore",
            "Feedback": "feedbackScore"
        }
        available_components = {
            label: column for label, column in component_columns.items() if column in df.columns
        }
        if available_components:
            component_df = df[list(available_components.values())].apply(
                pd.to_numeric, errors="coerce"
            )
            component_df.columns = list(available_components.keys())
            component_means = (
                component_df.mean()
                .rename_axis("Indicator")
                .reset_index(name="Average score")
                .dropna(subset=["Average score"])
            )
            if not component_means.empty:
                fig_components = px.bar(
                    component_means,
                    x="Indicator",
                    y="Average score",
                    range_y=[0, 100],
                    text_auto=".1f"
                )
                st.plotly_chart(fig_components, use_container_width=True, height=390)
            else:
                st.info("Enter indicator data to display category trends.")
        else:
            st.info("Category trend scores will appear after the backend is updated.")

        st.markdown("---")
        st.subheader("🏫 Department Comparison")
        department_summary = (
            df.assign(department=df['department'].fillna('Unknown'))
            .groupby('department', as_index=False)
            .agg(
                studentCount=('studentId', 'count'),
                avgSuccessScore=('successScore', 'mean')
            )
        )
        dept_count_col, dept_score_col = st.columns(2)

        with dept_count_col:
            st.caption("Students by Department")
            fig_dept_count = px.bar(
                department_summary,
                x='department',
                y='studentCount',
                labels={'department': 'Department', 'studentCount': 'Students'},
                text='studentCount'
            )
            st.plotly_chart(fig_dept_count, use_container_width=True, height=390)

        with dept_score_col:
            st.caption("Average Success Score by Department")
            fig_dept_score = px.bar(
                department_summary,
                x='department',
                y='avgSuccessScore',
                labels={'department': 'Department', 'avgSuccessScore': 'Average Score'},
                range_y=[0, 100],
                text_auto='.1f'
            )
            st.plotly_chart(fig_dept_score, use_container_width=True, height=390)

        st.markdown("---")
        st.subheader("Suggested Faculty Follow-up")
        risk_columns = [
            column for column in ("riskCategory", "academicRisk", "placementRisk")
            if column in df.columns
        ]
        follow_up_mask = pd.Series(False, index=df.index)
        for column in risk_columns:
            follow_up_mask |= df[column].isin(["High", "Moderate"])
        follow_up_columns = [
            column for column in (
                "studentId", "name", "department", "academicRisk", "placementRisk",
                "riskCategory", "actionableInsight"
            ) if column in df.columns
        ]
        if follow_up_mask.any() and follow_up_columns:
            follow_up_df = df.loc[follow_up_mask, follow_up_columns].copy()
            follow_up_df["Main drivers"] = df.loc[follow_up_mask].apply(
                lambda student: main_drivers(student.to_dict()),
                axis=1
            )
            st.dataframe(
                follow_up_df,
                use_container_width=True,
                hide_index=True,
                height=360
            )
        else:
            st.info("No high or moderate risk flags among the available indicators.")

elif page == "🧩 Student Segments & Explainable Insights":
    st.title("🧩 Student Segments & Explainable Insights")
    st.caption(
        "Rule-based student profiles surface contrasting strengths and support needs. "
        "They are decision-support groupings, not a trained clustering model."
    )
    if df.empty:
        if student_search.strip() and not all_students_df.empty:
            st.info(f"No students match “{student_search.strip()}”.")
        else:
            st.info("Student segments will appear when student records are available.")
    else:
        segment_records = []
        for student in df.to_dict("records"):
            scores = component_scores(student)
            segment_records.append({
                "Student ID": student.get("studentId", ""),
                "Student": student.get("name", ""),
                "Department": student.get("department", ""),
                "Semester": student.get("semester", ""),
                "Success score": student.get("successScore"),
                "Segment": student_segment(student),
                "Academic": scores.get("academic"),
                "Attendance": scores.get("attendance"),
                "Placement readiness": scores.get("placement"),
                "Academic risk": student.get("academicRisk", "Insufficient data"),
                "Placement risk": student.get("placementRisk", "Insufficient data"),
                "Main drivers": main_drivers(student)
            })
        segments_df = pd.DataFrame(segment_records)

        segment_counts = (
            segments_df["Segment"]
            .value_counts()
            .rename_axis("Student segment")
            .reset_index(name="Students")
        )
        st.subheader("Student segments")
        fig_segments = px.bar(
            segment_counts,
            x="Students",
            y="Student segment",
            orientation="h",
            text="Students",
            color="Student segment",
            color_discrete_sequence=CHART_COLORS
        )
        fig_segments.update_layout(showlegend=False, yaxis={"categoryorder": "total ascending"})
        st.plotly_chart(fig_segments, use_container_width=True, height=360)

        segment_options = ["All segments", *segment_counts["Student segment"].tolist()]
        selected_segment = st.selectbox("Filter by segment", segment_options)
        visible_segments = (
            segments_df
            if selected_segment == "All segments"
            else segments_df.loc[segments_df["Segment"] == selected_segment]
        )
        st.caption(f"Showing {len(visible_segments)} of {len(segments_df)} students.")
        st.dataframe(
            visible_segments,
            use_container_width=True,
            hide_index=True,
            height=480
        )

elif page == "📈 Weekly Student Activity":
    st.title("📈 Weekly Student Portal Activity")
    st.caption(
        "Successful student portal logins and estimated active portal time over the last 7 days. "
        "Time is counted only while the portal tab is visible and recently used."
    )
    faculty_token = st.session_state.get("faculty_token")
    try:
        activity_records = get_weekly_student_activity(faculty_token)
    except requests.RequestException as error:
        st.error(f"Could not load weekly student activity: {error}")
    else:
        roster_columns = [
            column for column in ("studentId", "name", "department", "semester")
            if column in df.columns
        ]
        if "studentId" in roster_columns:
            activity_df = (
                df[roster_columns]
                .dropna(subset=["studentId"])
                .drop_duplicates(subset=["studentId"])
                .copy()
            )
        else:
            activity_df = pd.DataFrame(columns=roster_columns)
        weekly_df = pd.DataFrame(activity_records)
        if weekly_df.empty:
            weekly_df = pd.DataFrame(
                columns=["studentId", "loginCount", "activeSeconds", "activeHours"]
            )
        activity_df = activity_df.merge(weekly_df, on="studentId", how="left")
        activity_df["loginCount"] = (
            pd.to_numeric(activity_df["loginCount"], errors="coerce")
            .fillna(0)
            .astype(int)
        )
        activity_df["activeSeconds"] = (
            pd.to_numeric(activity_df["activeSeconds"], errors="coerce").fillna(0)
        )
        activity_df["activeHours"] = (
            pd.to_numeric(activity_df["activeHours"], errors="coerce").fillna(0)
        )

        total_logins = int(activity_df["loginCount"].sum())
        total_active_hours = activity_df["activeHours"].sum()
        login_metric, hours_metric = st.columns(2)
        login_metric.metric("Student logins (7 days)", total_logins)
        hours_metric.metric(
            "Active portal hours (7 days)",
            f"{total_active_hours:.2f} h"
        )

        display_columns = [
            column for column in (
                "studentId", "name", "department", "semester",
                "loginCount", "activeHours"
            ) if column in activity_df.columns
        ]
        display_df = activity_df[display_columns].rename(columns={
            "studentId": "Student ID",
            "name": "Student",
            "department": "Department",
            "semester": "Semester",
            "loginCount": "Logins (7 days)",
            "activeHours": "Active hours (7 days)"
        })
        display_df["Active hours (7 days)"] = (
            display_df["Active hours (7 days)"].round(2)
        )
        display_df = display_df.sort_values(
            ["Logins (7 days)", "Active hours (7 days)"],
            ascending=False
        )
        st.dataframe(
            display_df,
            use_container_width=True,
            hide_index=True,
            height=520
        )
        st.caption(
            "Active time is an estimate based on a visible portal tab and recent user interaction; "
            "background and idle time are excluded."
        )

elif page == "💬 Feedback":
    st.title("💬 Feedback")
    st.caption(
        "Give feedback on a student, and review anonymous student feedback about staff."
    )
    faculty_token = st.session_state.get("faculty_token")

    if df.empty or "studentId" not in df.columns:
        if student_search.strip() and not all_students_df.empty:
            st.info(f"No students match “{student_search.strip()}”.")
        else:
            st.info("Add student records before giving student feedback.")
    else:
        feedback_students = (
            df[["studentId", "name", "department"]]
            .dropna(subset=["studentId"])
            .drop_duplicates(subset=["studentId"])
        )
        feedback_student_ids = feedback_students["studentId"].astype(str).tolist()
        feedback_student_labels = {
            str(row.studentId): (
                f"{row.name} — {row.studentId} ({row.department})"
            )
            for row in feedback_students.itertuples(index=False)
        }
        with st.form("faculty_student_feedback_form", clear_on_submit=True):
            selected_student_id = st.selectbox(
                "Student",
                feedback_student_ids,
                format_func=lambda student_id: feedback_student_labels[student_id]
            )
            rating = st.selectbox(
                "Overall rating",
                [5, 4, 3, 2, 1],
                format_func=lambda value: f"{value} / 5"
            )
            comment = st.text_area("Feedback (optional)", max_chars=1000)
            submitted = st.form_submit_button("Save feedback", type="primary")

        if submitted:
            try:
                submit_student_feedback(
                    faculty_token,
                    selected_student_id,
                    rating,
                    comment.strip()
                )
            except requests.RequestException as error:
                st.error(f"Could not save student feedback: {error}")
            else:
                st.success(
                    "Feedback saved. Your latest rating is included in the student's "
                    "feedback indicator."
                )
                st.rerun()

    st.subheader("Your feedback on students")
    try:
        my_feedback = get_my_student_feedback(faculty_token)
    except requests.RequestException as error:
        st.error(f"Could not load your student feedback: {error}")
    else:
        if my_feedback:
            feedback_df = pd.DataFrame(my_feedback).rename(
                columns={
                    "studentId": "Student ID",
                    "rating": "Rating (out of 5)",
                    "comment": "Feedback",
                    "updatedAt": "Updated"
                }
            )
            if not all_students_df.empty:
                names_by_id = all_students_df.set_index(
                    all_students_df["studentId"].astype(str)
                )["name"].to_dict()
                feedback_df.insert(
                    1,
                    "Student name",
                    feedback_df["Student ID"].astype(str).map(names_by_id).fillna("")
                )
            st.dataframe(feedback_df, use_container_width=True, hide_index=True)
        else:
            st.info("You have not submitted student feedback yet.")

    st.subheader("Anonymous student feedback about staff")
    try:
        staff_feedback = get_anonymous_staff_feedback(faculty_token)
    except requests.RequestException as error:
        st.error(f"Could not load anonymous staff feedback: {error}")
    else:
        if staff_feedback:
            staff_feedback_df = pd.DataFrame(staff_feedback).rename(
                columns={
                    "staffName": "Staff",
                    "rating": "Rating (out of 5)",
                    "comment": "Comment"
                }
            )
            st.dataframe(staff_feedback_df, use_container_width=True, hide_index=True)
        else:
            st.info("No anonymous student feedback has been submitted yet.")

elif page == "📚 Individual Course Grades":
    st.title("📚 Individual Course Grades")
    st.caption("Add and review course grades separately for each student.")

    if df.empty or "studentId" not in df.columns:
        if student_search.strip() and not all_students_df.empty:
            st.info(f"No students match “{student_search.strip()}”.")
        else:
            st.warning("Add student records before entering individual course grades.")
    else:
        student_records = (
            df[["studentId", "name", "department"]]
            .dropna(subset=["studentId"])
            .drop_duplicates(subset=["studentId"])
        )
        student_ids = student_records["studentId"].astype(str).tolist()
        student_departments = {
            str(row.studentId): (
                str(row.department)
                if pd.notna(row.department) and str(row.department) in DEPARTMENTS
                else "CSE"
            )
            for row in student_records.itertuples(index=False)
        }
        student_labels = {
            str(row.studentId): (
                f"{row.name} — {row.studentId} ({student_departments[str(row.studentId)]})"
            )
            for row in student_records.itertuples(index=False)
        }
        selected_student_id = st.selectbox(
            "Select student",
            student_ids,
            format_func=lambda student_id: student_labels[student_id]
        )
        selected_department = student_departments[selected_student_id]
        if selected_department == "IT":
            st.caption(
                "Semesters 1–2 use the common courses you supplied; semesters 3–8 "
                "use your supplied IT course list."
            )
        else:
            st.caption(
                f"Semesters 1–2 use the common courses you supplied; semesters 3–8 "
                f"use suggested {selected_department} courses. You can enter an unlisted course manually."
            )
        selected_semester = st.selectbox(
            "Select semester",
            range(1, 9),
            format_func=lambda semester: f"Semester {semester}",
            key="course_grade_semester"
        )

        try:
            course_grades = get_course_grades(selected_student_id)
        except requests.RequestException as exc:
            st.error(f"Could not load course grades: {exc}")
            course_grades = None

        if course_grades is not None:
            semester_courses = semester_course_options(
                selected_department, selected_semester
            )
            course_choice = st.selectbox(
                f"Course in Semester {selected_semester}",
                [*semester_courses, OTHER_COURSE],
                format_func=lambda course: (
                    f"{course[0]} — {course[1]}" if course[0] else course[1]
                ),
                key=f"course_choice_semester_{selected_semester}"
            )
            is_custom_course = course_choice == OTHER_COURSE

            with st.form("add_course_grade_form", clear_on_submit=True):
                if not is_custom_course:
                    course_code = course_choice[0] or ""
                    course_name = course_choice[1]
                    selected_course_label = (
                        f"{course_code} — {course_name}" if course_code else course_name
                    )
                    st.text_input(
                        "Selected course",
                        value=selected_course_label,
                        disabled=True
                    )
                else:
                    course_name = st.text_input("Course name")
                    course_code = st.text_input("Course code (optional)")

                selected_grade = st.selectbox("Grade", GRADE_OPTIONS)
                grade = (
                    st.text_input("Enter grade")
                    if selected_grade == "Other / enter manually"
                    else selected_grade
                )
                add_grade = st.form_submit_button("Save course grade")

                if add_grade:
                    if is_custom_course and not course_name.strip():
                        st.error("Select a listed course or enter a course name.")
                    elif not grade.strip():
                        st.error("Select a grade or enter one manually.")
                    else:
                        try:
                            add_course_grade(
                                selected_student_id,
                                {
                                    "semester": selected_semester,
                                    "courseName": course_name.strip(),
                                    "courseCode": course_code.strip() or None,
                                    "grade": grade.strip()
                                }
                            )
                        except requests.RequestException as exc:
                            st.error(f"Could not save course grade: {exc}")
                        else:
                            st.success("Course grade saved.")
                            st.rerun()

            st.subheader("Saved courses for this student")
            if course_grades:
                grades_df = pd.DataFrame(course_grades).rename(
                    columns={
                        "semester": "Semester",
                        "courseName": "Course",
                        "courseCode": "Course code",
                        "grade": "Grade"
                    }
                )
                st.dataframe(
                    grades_df[["Semester", "Course", "Course code", "Grade"]],
                    use_container_width=True,
                    hide_index=True,
                    height=320
                )

                grade_ids = [record["id"] for record in course_grades]
                grade_labels = {
                    record["id"]: (
                        f"Semester {record.get('semester') or '?'} — {record['courseName']} "
                        f"({record.get('courseCode') or 'no code'}) — {record['grade']}"
                    )
                    for record in course_grades
                }
                selected_grade_id = st.selectbox(
                    "Select a course grade to remove",
                    grade_ids,
                    format_func=lambda grade_id: grade_labels[grade_id]
                )
                if st.button("Delete selected course grade", type="secondary"):
                    try:
                        delete_course_grade(selected_student_id, selected_grade_id)
                    except requests.RequestException as exc:
                        st.error(f"Could not delete course grade: {exc}")
                    else:
                        st.success("Course grade deleted.")
                        st.rerun()
            else:
                st.info("No course grades have been added for this student yet.")

elif page == "👥 Saved Student Details":
    st.title("👥 Saved Student Details")

    st.subheader("Upload Student Data")
    template = pd.DataFrame(columns=STUDENT_CSV_COLUMNS).to_csv(index=False).encode("utf-8")
    st.download_button(
        "Download CSV template",
        data=template,
        file_name="student_upload_template.csv",
        mime="text/csv"
    )
    uploaded_file = st.file_uploader(
        "Choose a student CSV file",
        type=["csv"],
        help="Required columns: " + ", ".join(STUDENT_CSV_COLUMNS)
    )

    if uploaded_file is not None:
        try:
            upload_df = pd.read_csv(uploaded_file, dtype={"studentId": "string"})
        except (pd.errors.EmptyDataError, pd.errors.ParserError, UnicodeDecodeError) as exc:
            st.error(f"Could not read the CSV file: {exc}")
        else:
            missing_columns = [
                column for column in REQUIRED_STUDENT_COLUMNS
                if column not in upload_df.columns
            ]
            if missing_columns:
                st.error("Missing required CSV columns: " + ", ".join(missing_columns))
            elif upload_df.empty:
                st.warning("The CSV file has no student rows.")
            else:
                st.caption(f"Preview: {len(upload_df)} student row(s)")
                st.dataframe(
                    upload_df.head(10),
                    use_container_width=True,
                    hide_index=True,
                    height=360
                )

                if st.button("Import students", type="primary"):
                    existing_ids = set()
                    if not all_students_df.empty and "studentId" in all_students_df.columns:
                        existing_ids = {
                            str(value).strip()
                            for value in all_students_df["studentId"].dropna()
                        }

                    seen_ids = set(existing_ids)
                    imported_count = 0
                    import_errors = []

                    for index, row in upload_df.iterrows():
                        row_number = index + 2
                        student_id = "" if pd.isna(row["studentId"]) else str(row["studentId"]).strip()
                        name = "" if pd.isna(row["name"]) else str(row["name"]).strip()
                        department = "" if pd.isna(row["department"]) else str(row["department"]).strip()
                        row_errors = []

                        if not student_id or student_id == "<NA>":
                            row_errors.append("studentId is required")
                        elif student_id in seen_ids:
                            row_errors.append(f"studentId '{student_id}' already exists")
                        if not name or name.lower() == "nan":
                            row_errors.append("name is required")
                        if not department or department.lower() == "nan":
                            row_errors.append("department is required")

                        numeric_values = {field: None for field in NUMERIC_FIELDS}
                        for field, (minimum, maximum, must_be_integer) in NUMERIC_FIELDS.items():
                            if field not in upload_df.columns or pd.isna(row[field]):
                                if field in REQUIRED_STUDENT_COLUMNS:
                                    row_errors.append(f"{field} is required")
                                continue
                            try:
                                value = float(row[field])
                                if pd.isna(value) or value < minimum or value > maximum:
                                    raise ValueError
                                if must_be_integer and not value.is_integer():
                                    raise ValueError
                                numeric_values[field] = int(value) if must_be_integer else value
                            except (TypeError, ValueError):
                                row_errors.append(
                                    f"{field} must be "
                                    f"{'an integer ' if must_be_integer else ''}between "
                                    f"{minimum} and {maximum}"
                                )

                        if row_errors:
                            import_errors.append(f"Row {row_number}: " + "; ".join(row_errors))
                            continue

                        seen_ids.add(student_id)
                        payload = {
                            "studentId": student_id,
                            "name": name,
                            "department": department,
                            **numeric_values
                        }
                        try:
                            add_student(payload)
                        except requests.RequestException as exc:
                            import_errors.append(f"Row {row_number} ({student_id}): {exc}")
                        else:
                            imported_count += 1

                    st.success(f"Imported {imported_count} student(s).")
                    if import_errors:
                        st.warning(f"{len(import_errors)} row(s) could not be imported.")
                        for error in import_errors:
                            st.write(f"- {error}")
                    refreshed_students = get_all_students()
                    all_students_df = (
                        pd.DataFrame(refreshed_students)
                        if refreshed_students else pd.DataFrame()
                    )
                    df = filter_students(all_students_df, student_search)

    st.markdown("---")
    if df.empty:
        if student_search.strip() and not all_students_df.empty:
            st.info(f"No students match “{student_search.strip()}”.")
        else:
            st.warning("No student records found or Spring Boot API is offline.")
    else:
        st.caption(f"Total saved students: {len(df)}")
        student_columns = [
            "id", "studentId", "name", "department", "semester", "gpa",
            "internalAssessmentAvg", "backlogs", "subjectPerformanceAvg",
            "attendancePct", "subjectAttendanceAvg", "lmsActivityHrs", "lmsLoginFrequency",
            "assignmentCompletionPct", "eventsParticipated", "clubsParticipated",
            "hackathonsParticipated", "certifications", "aptitudeScore", "codingScore",
            "mockInterviewScore", "technicalSkillScore", "softSkillScore",
            "studentSatisfactionScore", "facultyFeedbackScore", "academicScore",
            "lmsScore", "engagementScore", "placementReadinessScore", "skillsScore",
            "feedbackScore", "successScore", "riskCategory", "academicRisk",
            "placementRisk", "dataCoveragePct", "actionableInsight"
        ]
        available_columns = [column for column in student_columns if column in df.columns]
        st.dataframe(
            df[available_columns],
            use_container_width=True,
            hide_index=True,
            height=480
        )

elif page == "➕ Add Student Profile":
    st.title("➕ Add New Student Record")
    
    with st.form("add_student_form"):
        student_id = st.text_input("Student ID")
        name = st.text_input("Student Name")
        department = st.selectbox("Department", DEPARTMENTS)
        semester = st.number_input("Semester", min_value=1, max_value=8, value=4)

        with st.expander("Academic indicators", expanded=True):
            cols = st.columns(4)
            gpa = optional_number_input(cols[0], "CGPA (0-10)", "gpa")
            internal_assessment = optional_number_input(
                cols[1], "Internal assessment average (0-100)", "internalAssessmentAvg"
            )
            backlogs = optional_number_input(cols[2], "Backlogs", "backlogs")
            subject_performance = optional_number_input(
                cols[3], "Subject performance average (0-100)", "subjectPerformanceAvg"
            )

        with st.expander("Attendance and LMS indicators", expanded=True):
            cols = st.columns(4)
            attendance_pct = optional_number_input(cols[0], "Overall attendance %", "attendancePct")
            subject_attendance = optional_number_input(
                cols[1], "Subject-wise average attendance %", "subjectAttendanceAvg"
            )
            lms_hrs = optional_number_input(cols[2], "LMS activity hours/week", "lmsActivityHrs")
            lms_logins = optional_number_input(cols[3], "LMS logins/week", "lmsLoginFrequency")
            assignment_completion = optional_number_input(
                st, "Assignment completion %", "assignmentCompletionPct"
            )

        with st.expander("Engagement indicators"):
            cols = st.columns(4)
            events = optional_number_input(cols[0], "Events participated", "eventsParticipated")
            clubs = optional_number_input(cols[1], "Clubs participated", "clubsParticipated")
            hackathons = optional_number_input(
                cols[2], "Hackathons participated", "hackathonsParticipated"
            )
            certifications = optional_number_input(cols[3], "Certifications", "certifications")

        with st.expander("Placement and skills indicators"):
            cols = st.columns(5)
            aptitude = optional_number_input(cols[0], "Aptitude score (0-100)", "aptitudeScore")
            coding = optional_number_input(cols[1], "Coding score (0-100)", "codingScore")
            mock_interview = optional_number_input(
                cols[2], "Mock interview score (0-100)", "mockInterviewScore"
            )
            technical_skills = optional_number_input(
                cols[3], "Technical skills (0-100)", "technicalSkillScore"
            )
            soft_skills = optional_number_input(
                cols[4], "Soft skills (0-100)", "softSkillScore"
            )

        with st.expander("Feedback indicators", expanded=True):
            st.caption("Optional: type a feedback score from 0 to 100.")
            cols = st.columns(2)
            student_satisfaction = optional_feedback_input(
                cols[0], "Student satisfaction (0-100)", "studentSatisfactionScore"
            )
            faculty_feedback = optional_feedback_input(
                cols[1], "Faculty feedback (0-100)", "facultyFeedbackScore"
            )

        submit = st.form_submit_button("Submit & Calculate Risk")

        if submit:
            if not student_id.strip() or not name.strip():
                st.error("Student ID and student name are required.")
                st.stop()

            try:
                numeric_inputs = {
                    "gpa": (gpa, "CGPA"),
                    "internalAssessmentAvg": (internal_assessment, "Internal assessment average"),
                    "backlogs": (backlogs, "Backlogs"),
                    "subjectPerformanceAvg": (subject_performance, "Subject performance average"),
                    "attendancePct": (attendance_pct, "Overall attendance"),
                    "subjectAttendanceAvg": (subject_attendance, "Subject-wise average attendance"),
                    "lmsActivityHrs": (lms_hrs, "LMS activity hours/week"),
                    "lmsLoginFrequency": (lms_logins, "LMS logins/week"),
                    "assignmentCompletionPct": (assignment_completion, "Assignment completion"),
                    "eventsParticipated": (events, "Events participated"),
                    "clubsParticipated": (clubs, "Clubs participated"),
                    "hackathonsParticipated": (hackathons, "Hackathons participated"),
                    "certifications": (certifications, "Certifications"),
                    "aptitudeScore": (aptitude, "Aptitude score"),
                    "codingScore": (coding, "Coding score"),
                    "mockInterviewScore": (mock_interview, "Mock interview score"),
                    "technicalSkillScore": (technical_skills, "Technical skills"),
                    "softSkillScore": (soft_skills, "Soft skills")
                }
                parsed_numeric_inputs = {
                    field: parse_optional_number(value, field, label)
                    for field, (value, label) in numeric_inputs.items()
                }
                student_satisfaction_score = parse_feedback_score(
                    student_satisfaction, "Student satisfaction"
                )
                faculty_feedback_score = parse_feedback_score(
                    faculty_feedback, "Faculty feedback"
                )
            except ValueError as exc:
                st.error(str(exc))
                st.stop()

            payload = {
                "studentId": student_id.strip(),
                "name": name.strip(),
                "department": department,
                "semester": semester,
                **parsed_numeric_inputs,
                "studentSatisfactionScore": student_satisfaction_score,
                "facultyFeedbackScore": faculty_feedback_score
            }
            try:
                add_student(payload)
            except requests.RequestException as exc:
                st.error(f"Failed to add student: {exc}")
            else:
                st.success("Student added successfully! Risk score calculated.")