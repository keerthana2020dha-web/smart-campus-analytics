import requests

API_URL = "http://localhost:8081/api/v1/faculty-portal"


def login_faculty(faculty_id, password):
    response = requests.post(
        f"{API_URL}/auth/login",
        json={"facultyId": faculty_id, "password": password},
        timeout=10
    )
    response.raise_for_status()
    return response.json()


def logout_faculty(token):
    return requests.post(
        f"{API_URL}/auth/logout",
        headers={"Authorization": f"Bearer {token}"},
        timeout=10
    )


def submit_student_feedback(token, student_id, rating, comment):
    response = requests.post(
        f"{API_URL}/feedback",
        headers={"Authorization": f"Bearer {token}"},
        json={
            "studentId": student_id,
            "rating": rating,
            "comment": comment or None
        },
        timeout=10
    )
    response.raise_for_status()
    return response.json()


def get_my_student_feedback(token):
    response = requests.get(
        f"{API_URL}/feedback/mine",
        headers={"Authorization": f"Bearer {token}"},
        timeout=10
    )
    response.raise_for_status()
    return response.json()


def get_anonymous_staff_feedback(token):
    response = requests.get(
        f"{API_URL}/feedback/staff",
        headers={"Authorization": f"Bearer {token}"},
        timeout=10
    )
    response.raise_for_status()
    return response.json()


def get_weekly_student_activity(token):
    response = requests.get(
        f"{API_URL}/student-activity/weekly",
        headers={"Authorization": f"Bearer {token}"},
        timeout=10
    )
    response.raise_for_status()
    return response.json()
