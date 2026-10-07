import requests
from urllib.parse import quote

BASE_URL = "http://localhost:8081/api/v1/students"
_faculty_token = None


def set_faculty_token(token):
    global _faculty_token
    _faculty_token = token


def _faculty_headers():
    if not _faculty_token:
        raise RuntimeError("A faculty session is required to access student records.")
    return {"Authorization": f"Bearer {_faculty_token}"}

def get_all_students():
    response = requests.get(BASE_URL, headers=_faculty_headers(), timeout=5)
    response.raise_for_status()
    return response.json()


def add_student(student_data):
    response = requests.post(
        BASE_URL, json=student_data, headers=_faculty_headers(), timeout=5
    )
    response.raise_for_status()
    return response.json()


def get_course_grades(student_id):
    student_path = quote(student_id, safe="")
    response = requests.get(
        f"{BASE_URL}/{student_path}/courses",
        headers=_faculty_headers(),
        timeout=5
    )
    response.raise_for_status()
    return response.json()


def add_course_grade(student_id, course_grade):
    student_path = quote(student_id, safe="")
    response = requests.post(
        f"{BASE_URL}/{student_path}/courses",
        json=course_grade,
        headers=_faculty_headers(),
        timeout=5
    )
    response.raise_for_status()
    return response.json()


def delete_course_grade(student_id, course_grade_id):
    student_path = quote(student_id, safe="")
    response = requests.delete(
        f"{BASE_URL}/{student_path}/courses/{course_grade_id}",
        headers=_faculty_headers(),
        timeout=5
    )
    response.raise_for_status()