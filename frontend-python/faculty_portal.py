import requests
import streamlit as st

from services.faculty_portal_service import login_faculty


def run_faculty_portal():
    st.title("Faculty Login")
    st.caption("Sign in with the faculty credentials provisioned by your administrator.")

    with st.form("faculty_login_form"):
        faculty_id = st.text_input("Faculty ID")
        password = st.text_input("Password", type="password")
        submitted = st.form_submit_button("Login", type="primary")

    if not submitted:
        return
    if not faculty_id.strip() or not password:
        st.error("Faculty ID and password are required.")
        return

    try:
        result = login_faculty(faculty_id.strip(), password)
    except requests.HTTPError as error:
        if error.response is not None and error.response.status_code == 401:
            st.error("Invalid faculty ID or password.")
        else:
            st.error(f"Faculty login failed: {error}")
    except requests.RequestException as error:
        st.error(f"Could not reach the faculty portal: {error}")
    else:
        st.session_state["faculty_token"] = result["accessToken"]
        st.rerun()
