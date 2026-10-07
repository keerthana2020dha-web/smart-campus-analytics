import importlib
import sys

import requests
import streamlit as st

from services.student_service import set_faculty_token
from student_portal import run_student_portal
from theme import apply_app_theme, apply_portal_selection_theme

st.set_page_config(page_title="Smart Campus Analytics", page_icon="🎓", layout="wide")
apply_app_theme()

portal = st.session_state.get("selected_portal")
if portal not in {"Student", "Faculty"}:
    apply_portal_selection_theme()
    st.markdown(
        """
        <div class="portal-selection-page"></div>
        <section class="portal-hero">
            <p class="portal-eyebrow">SMART CAMPUS ANALYTICS</p>
            <h1>Choose your space</h1>
            <p>One campus, two focused experiences. Select a portal to continue.</p>
        </section>
        """,
        unsafe_allow_html=True
    )
    left, student_column, faculty_column, right = st.columns([1, 3, 3, 1])

    with student_column:
        with st.container(border=True):
            st.markdown(
                """
                <div class="portal-card-content student-portal-card">
                    <span class="portal-card-icon">🎓</span>
                    <p class="portal-card-kicker">LEARNER ACCESS</p>
                    <h2>Student Portal</h2>
                    <p>Keep your campus journey in one place: activities, certificates,
                    and subject-wise assignments.</p>
                </div>
                """,
                unsafe_allow_html=True
            )
            if st.button(
                "Continue as Student",
                key="enter_student_portal",
                type="primary",
                width="stretch"
            ):
                st.session_state["selected_portal"] = "Student"
                st.rerun()

    with faculty_column:
        with st.container(border=True):
            st.markdown(
                """
                <div class="portal-card-content faculty-portal-card">
                    <span class="portal-card-icon">📊</span>
                    <p class="portal-card-kicker">FACULTY ACCESS</p>
                    <h2>Faculty Portal</h2>
                    <p>Explore student success insights, academic trends, and
                    data-informed follow-up actions.</p>
                </div>
                """,
                unsafe_allow_html=True
            )
            if st.button(
                "Continue as Faculty",
                key="enter_faculty_portal",
                type="primary",
                width="stretch"
            ):
                st.session_state["selected_portal"] = "Faculty"
                st.rerun()

    st.stop()

if st.sidebar.button("Choose another portal"):
    st.session_state.pop("selected_portal", None)
    st.rerun()

if portal == "Student":
    run_student_portal()
else:
    from faculty_portal import run_faculty_portal

    faculty_token = st.session_state.get("faculty_token")
    if faculty_token:
        set_faculty_token(faculty_token)
        if st.sidebar.button("Faculty logout"):
            try:
                from services.faculty_portal_service import logout_faculty

                logout_faculty(faculty_token).raise_for_status()
            except requests.RequestException as error:
                st.error(f"Could not log out: {error}")
            st.session_state.pop("faculty_token", None)
            set_faculty_token(None)
            st.rerun()
        try:
            if "faculty_dashboard" in sys.modules:
                importlib.reload(sys.modules["faculty_dashboard"])
            else:
                importlib.import_module("faculty_dashboard")
        except requests.HTTPError as error:
            if error.response is not None and error.response.status_code == 401:
                st.session_state.pop("faculty_token", None)
                set_faculty_token(None)
                st.warning("Your faculty session has expired. Please sign in again.")
                st.rerun()
            st.error(f"Could not load the faculty dashboard: {error}")
        except requests.RequestException as error:
            st.error(f"Could not reach the faculty dashboard API: {error}")
    else:
        run_faculty_portal()
