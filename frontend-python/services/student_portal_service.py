import requests

API_URL = "http://localhost:8081/api/v1/student-portal"


def login_student(student_id, password):
    response = requests.post(
        f"{API_URL}/auth/login",
        json={"studentId": student_id, "password": password},
        timeout=10
    )
    response.raise_for_status()
    return response.json()


def logout_student(token):
    return requests.post(
        f"{API_URL}/auth/logout",
        headers={"Authorization": f"Bearer {token}"},
        timeout=10
    )


def get_my_profile(token):
    return _request("get", "/me", token).json()


def submit_staff_feedback(token, staff_name, rating, comment):
    return _request(
        "post",
        "/feedback",
        token,
        json={
            "staffName": staff_name,
            "rating": rating,
            "comment": comment or None
        }
    )


def get_my_activities(token):
    return _request("get", "/me/activities", token).json()


def add_my_activity(token, activity):
    return _request("post", "/me/activities", token, json=activity).json()


def get_my_certificates(token):
    return _request("get", "/me/certificates", token).json()


def upload_my_certificate(token, title, certificate_type, issued_date, uploaded_file):
    data = {"title": title, "certificateType": certificate_type}
    if issued_date:
        data["issuedDate"] = issued_date
    return _request(
        "post",
        "/me/certificates",
        token,
        data=data,
        files={
            "file": (
                uploaded_file.name,
                uploaded_file.getvalue(),
                uploaded_file.type or "application/octet-stream"
            )
        }
    ).json()


def download_my_certificate(token, certificate_id):
    return _request(
        "get", f"/me/certificates/{certificate_id}/file", token
    ).content


def get_my_assignment_submissions(token):
    return _request("get", "/me/assignment-submissions", token).json()


def submit_my_assignment(
    token, semester, course_name, course_code, assignment_title, uploaded_file
):
    data = {
        "semester": str(semester),
        "courseName": course_name,
        "assignmentTitle": assignment_title
    }
    if course_code:
        data["courseCode"] = course_code
    return _request(
        "post",
        "/me/assignment-submissions",
        token,
        data=data,
        files={
            "file": (
                uploaded_file.name,
                uploaded_file.getvalue(),
                uploaded_file.type or "application/octet-stream"
            )
        }
    ).json()


def download_my_assignment(token, submission_id):
    return _request(
        "get", f"/me/assignment-submissions/{submission_id}/file", token
    ).content


def _request(method, path, token, **kwargs):
    response = requests.request(
        method,
        f"{API_URL}{path}",
        headers={"Authorization": f"Bearer {token}"},
        timeout=30,
        **kwargs
    )
    response.raise_for_status()
    return response
