from datetime import date

import pandas as pd
import requests
import streamlit as st
import streamlit.components.v1 as components

from course_catalog import courses_for
from services.student_portal_service import (
    add_my_activity,
    download_my_assignment,
    download_my_certificate,
    get_my_activities,
    get_my_assignment_submissions,
    get_my_certificates,
    get_my_profile,
    login_student,
    logout_student,
    submit_staff_feedback,
    submit_my_assignment,
    upload_my_certificate
)

def _show_request_error(message, error):
    response = getattr(error, "response", None)
    if response is not None and response.status_code == 401:
        st.session_state.pop("student_token", None)
        st.error("Your login has expired or is invalid. Please sign in again.")
        st.rerun()
    st.error(f"{message}: {error}")


def _format_date(value):
    return value.isoformat() if value else None


def _track_active_portal_time(token):
    components.html(
        f"""
        <script>
        const token = "{token}";
        const heartbeatUrl =
            "http://localhost:8081/api/v1/student-portal/activity/heartbeat";
        const activityWindow = window.parent;
        const activityDocument = activityWindow.document;
        let lastInteraction = Date.now();
        const markActive = () => {{ lastInteraction = Date.now(); }};
        ["pointerdown", "keydown", "mousemove", "scroll", "touchstart"]
            .forEach(eventName => activityDocument.addEventListener(
                eventName, markActive, {{passive: true, capture: true}}
            ));

        async function sendHeartbeat() {{
            const active = activityDocument.visibilityState === "visible"
                && Date.now() - lastInteraction <= 90000;
            try {{
                const response = await fetch(heartbeatUrl, {{
                    method: "POST",
                    headers: {{
                        "Authorization": `Bearer ${{token}}`,
                        "Content-Type": "application/json"
                    }},
                    body: JSON.stringify({{active}})
                }});
                if (!response.ok) {{
                    console.error("Student activity heartbeat failed:", response.status);
                }}
            }} catch (error) {{
                console.error("Student activity heartbeat could not reach the API:", error);
            }}
        }}

        sendHeartbeat();
        window.setInterval(sendHeartbeat, 30000);
        activityDocument.addEventListener("visibilitychange", () => {{
            if (activityDocument.visibilityState !== "visible") {{
                lastInteraction = 0;
                sendHeartbeat();
            }}
        }});
        </script>
        """,
        height=0
    )


def _show_login():
    st.title("🎓 Student Login")
    st.caption("Sign in using your registration number and date of birth.")

    with st.form("student_login_form"):
        student_id = st.text_input("Registration number")
        password = st.text_input(
            "Date of birth",
            type="password",
            placeholder="YYYY-MM-DD",
            help="Enter your DOB in YYYY-MM-DD format."
        )
        submitted = st.form_submit_button("Login", type="primary")

    if submitted:
        if not student_id.strip() or not password:
            st.error("Registration number and date of birth are required.")
            return
        try:
            result = login_student(student_id.strip(), password.strip())
        except requests.HTTPError as error:
            if error.response is not None and error.response.status_code == 401:
                st.error("Invalid registration number or date of birth.")
            else:
                _show_request_error("Login failed", error)
        except requests.RequestException as error:
            _show_request_error("Could not reach the student portal", error)
        else:
            st.session_state["student_token"] = result["accessToken"]
            st.rerun()


def _render_home(profile):
    st.title(f"Welcome, {profile['name']}")
    st.caption(
        f"Registration number: {profile['studentId']}  ·  "
        f"Department: {profile.get('department') or 'Not set'}  ·  "
        f"Semester: {profile.get('semester') or 'Not set'}"
    )
    st.subheader("Your activity summary")
    first, second, third, fourth = st.columns(4)
    first.metric("Portal logins", profile["loginCount"])
    second.metric("Hackathons", profile["hackathonCount"])
    third.metric("Events", profile["eventCount"])
    fourth.metric("Clubs", profile["clubCount"])
    first, second = st.columns(2)
    first.metric("Uploaded certificates", profile["certificateCount"])
    second.metric("Assignment submissions", profile["assignmentSubmissionCount"])
    st.info("This portal only shows and updates records belonging to your account.")


def _render_feedback(token):
    st.title("💬 Feedback for Staff")
    st.caption(
        "Share a respectful, constructive rating or comment about a staff member. "
        "Your registration number is not attached to this feedback."
    )

    with st.form("student_staff_feedback_form", clear_on_submit=True):
        staff_name = st.text_input("Staff name", max_chars=120)
        rating = st.selectbox(
            "Overall rating",
            [5, 4, 3, 2, 1],
            format_func=lambda value: f"{value} / 5"
        )
        comment = st.text_area("Comments (optional)", max_chars=1000)
        submitted = st.form_submit_button("Submit anonymous feedback", type="primary")

    if not submitted:
        return
    if not staff_name.strip():
        st.error("Enter the staff member's name.")
        return

    try:
        submit_staff_feedback(token, staff_name.strip(), rating, comment.strip())
    except requests.RequestException as error:
        _show_request_error("Could not submit feedback", error)
    else:
        st.success("Your anonymous feedback was submitted.")


def _render_activities(token):
    st.title("🏆 Hackathons, Events & Clubs")
    st.caption("Record each activity you participated in.")

    with st.form("student_activity_form", clear_on_submit=True):
        activity_type = st.selectbox("Activity type", ["Hackathon", "Event", "Club"])
        activity_name = st.text_input("Activity name")
        has_date = st.checkbox("Add activity date")
        activity_date = st.date_input("Activity date", value=date.today()) if has_date else None
        description = st.text_area("Description (optional)", max_chars=500)
        submitted = st.form_submit_button("Save activity", type="primary")

    if submitted:
        if not activity_name.strip():
            st.error("Enter the activity name.")
        else:
            try:
                add_my_activity(
                    token,
                    {
                        "activityType": activity_type.upper(),
                        "activityName": activity_name.strip(),
                        "activityDate": _format_date(activity_date),
                        "description": description.strip() or None
                    }
                )
            except requests.RequestException as error:
                _show_request_error("Could not save activity", error)
            else:
                st.success("Activity saved.")
                st.rerun()

    try:
        activities = get_my_activities(token)
    except requests.RequestException as error:
        _show_request_error("Could not load your activities", error)
        return
    if activities:
        st.dataframe(
            pd.DataFrame(activities).rename(
                columns={
                    "activityType": "Type",
                    "activityName": "Activity",
                    "activityDate": "Date",
                    "description": "Description"
                }
            )[["Type", "Activity", "Date", "Description"]],
            use_container_width=True,
            hide_index=True
        )
    else:
        st.info("No activities have been added yet.")


def _render_certificates(token):
    st.title("📜 Certificates")
    st.caption(
        "Upload certificates for courses, certifications, events, hackathons, or clubs. "
        "The count is calculated from distinct uploaded files."
    )

    with st.form("student_certificate_form", clear_on_submit=True):
        title = st.text_input("Certificate title")
        certificate_type = st.selectbox(
            "Certificate category",
            ["Certification", "Course", "Hackathon", "Event", "Club", "Other"]
        )
        has_issue_date = st.checkbox("Add issue date")
        issued_date = st.date_input("Issue date", value=date.today()) if has_issue_date else None
        uploaded_file = st.file_uploader(
            "Upload certificate (PDF, JPG, PNG; max 10 MB)",
            type=["pdf", "jpg", "jpeg", "png"],
            max_upload_size=10,
            key="certificate_file"
        )
        submitted = st.form_submit_button("Upload certificate", type="primary")

    if submitted:
        if not title.strip() or uploaded_file is None:
            st.error("Enter a certificate title and choose a certificate file.")
        else:
            try:
                upload_my_certificate(
                    token,
                    title.strip(),
                    certificate_type.upper(),
                    _format_date(issued_date),
                    uploaded_file
                )
            except requests.HTTPError as error:
                response = error.response
                if response is not None and response.status_code == 409:
                    st.error("This certificate file has already been uploaded.")
                else:
                    _show_request_error("Could not upload certificate", error)
            except requests.RequestException as error:
                _show_request_error("Could not upload certificate", error)
            else:
                st.success("Certificate uploaded. Your certificate count has been updated.")
                st.rerun()

    try:
        certificates = get_my_certificates(token)
    except requests.RequestException as error:
        _show_request_error("Could not load your certificates", error)
        return
    if not certificates:
        st.info("No certificates have been uploaded yet.")
        return

    for certificate in certificates:
        with st.container(border=True):
            st.write(
                f"**{certificate['title']}** · {certificate['certificateType']} · "
                f"{certificate['originalFileName']}"
            )
            if certificate.get("issuedDate"):
                st.caption(f"Issue date: {certificate['issuedDate']}")
            try:
                file_bytes = download_my_certificate(token, certificate["id"])
            except requests.RequestException as error:
                _show_request_error("Could not load certificate file", error)
            else:
                st.download_button(
                    "Download certificate",
                    data=file_bytes,
                    file_name=certificate["originalFileName"],
                    key=f"download_certificate_{certificate['id']}"
                )


def _render_assignments(token, profile):
    st.title("📝 Subject-wise Assignments")
    st.caption("Choose a semester and subject, then upload your assignment file.")
    semester_default = profile.get("semester")
    if not isinstance(semester_default, int) or not 1 <= semester_default <= 8:
        semester_default = 1
    semester = st.selectbox(
        "Semester",
        range(1, 9),
        index=semester_default - 1,
        format_func=lambda value: f"Semester {value}",
        key="assignment_semester"
    )
    department = profile.get("department")
    if department == "IT" or department in {
        "CSE", "ECE", "EEE", "MECH", "CIVIL", "AI & Data Science",
        "Artificial Intelligence & Machine Learning", "Cyber Security",
        "Biotechnology", "Biomedical Engineering", "Chemical Engineering",
        "Aeronautical Engineering", "Automobile Engineering",
        "Agricultural Engineering", "Mechatronics", "Food Technology",
        "Textile Technology"
    }:
        courses = courses_for(department, semester)
    else:
        st.error("Your department is not set up for the subject list. Contact your college.")
        return

    course_choices = list(enumerate(courses))
    selected_index, selected_course = st.selectbox(
        "Subject",
        course_choices,
        format_func=lambda entry: (
            f"{entry[1][0]} — {entry[1][1]}" if entry[1][0] else entry[1][1]
        ),
        key=f"assignment_course_{semester}"
    )
    del selected_index

    with st.form("student_assignment_form", clear_on_submit=True):
        assignment_title = st.text_input("Assignment title")
        uploaded_file = st.file_uploader(
            "Upload assignment (PDF, JPG, PNG; max 10 MB)",
            type=["pdf", "jpg", "jpeg", "png"],
            max_upload_size=10,
            key="assignment_file"
        )
        submitted = st.form_submit_button("Submit assignment", type="primary")

    if submitted:
        if not assignment_title.strip() or uploaded_file is None:
            st.error("Enter the assignment title and choose a file.")
        else:
            try:
                submit_my_assignment(
                    token,
                    semester,
                    selected_course[1],
                    selected_course[0],
                    assignment_title.strip(),
                    uploaded_file
                )
            except requests.RequestException as error:
                _show_request_error("Could not submit assignment", error)
            else:
                st.success("Assignment submitted.")
                st.rerun()

    try:
        submissions = get_my_assignment_submissions(token)
    except requests.RequestException as error:
        _show_request_error("Could not load assignment submissions", error)
        return
    st.subheader("Your submitted assignments")
    if not submissions:
        st.info("No assignments have been submitted yet.")
        return
    for submission in submissions:
        with st.container(border=True):
            course_code = submission.get("courseCode")
            course_label = (
                f"{course_code} — {submission['courseName']}"
                if course_code else submission["courseName"]
            )
            st.write(
                f"**{submission['assignmentTitle']}** · Semester {submission['semester']} · "
                f"{course_label}"
            )
            st.caption(
                f"Submitted: {submission['uploadedAt']} · "
                f"File: {submission['originalFileName']}"
            )
            try:
                file_bytes = download_my_assignment(token, submission["id"])
            except requests.RequestException as error:
                _show_request_error("Could not load assignment file", error)
            else:
                st.download_button(
                    "Download submission",
                    data=file_bytes,
                    file_name=submission["originalFileName"],
                    key=f"download_assignment_{submission['id']}"
                )


def run_student_portal():
    token = st.session_state.get("student_token")
    if not token:
        _show_login()
        return

    _track_active_portal_time(token)

    try:
        profile = get_my_profile(token)
    except requests.RequestException as error:
        _show_request_error("Could not load your account", error)
        return

    st.sidebar.title("🎓 Student Portal")
    st.sidebar.caption(profile["studentId"])
    page = st.sidebar.radio(
        "Go to",
        [
            "My Summary",
            "Hackathons, Events & Clubs",
            "Certificates",
            "Subject-wise Assignments",
            "Feedback for Staff"
        ]
    )
    if st.sidebar.button("Logout"):
        try:
            logout_student(token).raise_for_status()
        except requests.RequestException as error:
            _show_request_error("Could not log out", error)
        st.session_state.pop("student_token", None)
        st.rerun()

    if page == "My Summary":
        _render_home(profile)
    elif page == "Hackathons, Events & Clubs":
        _render_activities(token)
    elif page == "Certificates":
        _render_certificates(token)
    elif page == "Subject-wise Assignments":
        _render_assignments(token, profile)
    elif page == "Feedback for Staff":
        _render_feedback(token)
