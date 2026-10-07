import streamlit as st

BURGUNDY = "#5A1A32"
ROSE = "#B24C63"
PALE_PINK = "#FDF0F4"
BLUSH = "#F8E3EA"
CHART_COLORS = [BURGUNDY, ROSE, "#D1788D", "#8D3B55", "#E5A6B5", "#754357"]


def apply_app_theme():
    st.markdown(
        """
        <style>
        [data-testid="stAppViewContainer"] {
            background: linear-gradient(145deg, #FDF0F4 0%, #FFF9FB 62%, #F8E3EA 100%);
        }
        [data-testid="stHeader"] { background: rgba(253, 240, 244, 0.88); }
        [data-testid="stHorizontalBlock"] { gap: 1.25rem; }
        [data-testid="stSidebar"] {
            background: linear-gradient(180deg, #241321 0%, #351628 58%, #241321 100%);
            border-right: 1px solid rgba(229, 166, 181, 0.24);
        }
        [data-testid="stSidebar"] [data-testid="stMarkdownContainer"] h1,
        [data-testid="stSidebar"] [data-testid="stMarkdownContainer"] h2,
        [data-testid="stSidebar"] [data-testid="stMarkdownContainer"] h3 {
            color: #FFF7FA;
            letter-spacing: -0.02em;
        }
        [data-testid="stSidebar"] p,
        [data-testid="stSidebar"] label,
        [data-testid="stSidebar"] [data-testid="stCaptionContainer"] {
            color: #E8D5DE;
        }
        [data-testid="stSidebar"] [data-testid="stCaptionContainer"] {
            color: #E5A6B5;
        }
        [data-testid="stSidebar"] [data-testid="stRadio"] [role="radiogroup"] {
            gap: 0.35rem;
        }
        [data-testid="stSidebar"] [data-testid="stRadio"] label[data-baseweb="radio"] {
            min-height: 2.8rem;
            padding: 0.55rem 0.7rem;
            border: 1px solid transparent;
            border-radius: 12px;
            background: rgba(255, 255, 255, 0.035);
            transition: background 140ms ease, border-color 140ms ease, transform 140ms ease;
        }
        [data-testid="stSidebar"] [data-testid="stRadio"] label[data-baseweb="radio"]:hover {
            border-color: rgba(229, 166, 181, 0.35);
            background: rgba(178, 76, 99, 0.18);
            transform: translateX(2px);
        }
        [data-testid="stSidebar"] [data-testid="stRadio"] label[data-baseweb="radio"]:has(input:checked) {
            border-color: rgba(229, 166, 181, 0.48);
            background: linear-gradient(100deg, rgba(178, 76, 99, 0.36), rgba(90, 26, 50, 0.58));
            box-shadow: inset 3px 0 0 #D1788D, 0 5px 14px rgba(8, 4, 12, 0.14);
        }
        [data-testid="stSidebar"] [data-testid="stRadio"] label[data-baseweb="radio"] p {
            color: #F8EAF0;
            font-weight: 550;
        }
        [data-testid="stSidebar"] [data-testid="stRadio"] label[data-baseweb="radio"]:has(input:checked) p {
            color: #FFFFFF;
            font-weight: 700;
        }
        [data-testid="stSidebar"] [data-testid="stTextInputRootElement"],
        [data-testid="stSidebar"] [data-baseweb="input"],
        [data-testid="stSidebar"] [data-baseweb="select"] > div {
            background: rgba(255, 255, 255, 0.09) !important;
            border-color: rgba(229, 166, 181, 0.38) !important;
            color: #FFF7FA !important;
        }
        [data-testid="stSidebar"] input,
        [data-testid="stSidebar"] [data-baseweb="select"] *,
        [data-testid="stSidebar"] [data-baseweb="input"] * {
            color: #FFF7FA !important;
        }
        [data-testid="stSidebar"] [data-testid="stTextInput"] input::placeholder {
            color: #D2B9C5 !important;
        }
        [data-testid="stSidebar"] [data-testid="stButton"] button {
            width: 100%;
            border: 1px solid rgba(229, 166, 181, 0.42);
            border-radius: 11px;
            background: rgba(178, 76, 99, 0.16);
            color: #FFF7FA;
            font-weight: 600;
            transition: background 140ms ease, border-color 140ms ease, transform 140ms ease;
        }
        [data-testid="stSidebar"] [data-testid="stButton"] button:not([kind="primary"]):hover,
        [data-testid="stSidebar"] [data-testid="stButton"] button[kind="primary"]:hover {
            border-color: #E5A6B5;
            background: linear-gradient(100deg, #8D3B55, #B24C63);
            color: #FFFFFF;
            transform: translateY(-1px);
        }
        [data-testid="stSidebar"] [data-testid="stButton"] button p {
            color: inherit;
            font-weight: inherit;
        }
        h1, h2, h3 { color: #5A1A32; }
        p, label, [data-testid="stCaptionContainer"] { color: #65364A; }
        [data-testid="stSubheader"] { margin-top: 1.6rem; }
        [data-testid="stMetric"] {
            background: linear-gradient(145deg, #FFFFFF 0%, #FFF7FA 100%);
            border: 1px solid #E7C4CF;
            border-radius: 12px;
            padding: 16px 18px;
            box-shadow: 0 4px 16px rgba(90, 26, 50, 0.08);
        }
        [data-testid="stMetricLabel"] { color: #754357; }
        [data-testid="stMetricValue"] { color: #5A1A32; }
        [data-testid="stPlotlyChart"] {
            background: rgba(255, 255, 255, 0.92);
            border: 1px solid #E7C4CF;
            border-radius: 12px;
            padding: 12px;
            margin: 0.65rem 0 1.25rem;
            min-width: 0;
        }
        [data-testid="stForm"] {
            background: rgba(255, 255, 255, 0.9);
            border: 1px solid #E7C4CF;
            border-radius: 12px;
            padding: 18px;
        }
        div[data-testid="stDataFrame"] {
            border: 1px solid #E7C4CF;
            border-radius: 10px;
            overflow: hidden;
            margin: 0.65rem 0 1.4rem;
        }
        [data-testid="stTextInput"] input,
        [data-testid="stTextArea"] textarea,
        [data-testid="stNumberInput"] input,
        [data-testid="stDateInput"] input,
        [data-testid="stSelectbox"] [data-baseweb="select"] > div {
            border-color: #D9A6B5;
            border-radius: 8px;
            color: #5A1A32;
        }
        [data-testid="stTextInputRootElement"],
        [data-testid="stNumberInput"] [data-baseweb="input"],
        [data-testid="stDateInput"] [data-baseweb="input"],
        [data-testid="stTextArea"] [data-baseweb="textarea"],
        [data-testid="stSelectbox"] [data-baseweb="select"] > div {
            background-color: #FFFFFF !important;
            border-color: #D9A6B5;
        }
        [data-testid="stTextInput"] input:focus,
        [data-testid="stTextArea"] textarea:focus,
        [data-testid="stNumberInput"] input:focus,
        [data-testid="stDateInput"] input:focus {
            border-color: #B24C63;
            box-shadow: 0 0 0 1px #B24C63;
        }
        [data-testid="stButton"] button,
        [data-testid="stFormSubmitButton"] button,
        [data-testid="stDownloadButton"] button {
            border-radius: 9px;
            border-color: #B24C63;
            transition: background-color 120ms ease, border-color 120ms ease;
        }
        [data-testid="stButton"] button[kind="primary"],
        [data-testid="stFormSubmitButton"] button[kind="primary"],
        [data-testid="stFormSubmitButton"] button[kind="primaryFormSubmit"] {
            background: #5A1A32;
            border-color: #5A1A32;
            color: #FFFFFF;
        }
        [data-testid="stButton"] button[kind="primary"]:hover,
        [data-testid="stFormSubmitButton"] button[kind="primary"]:hover,
        [data-testid="stFormSubmitButton"] button[kind="primaryFormSubmit"]:hover {
            background: #B24C63;
            border-color: #B24C63;
            color: #FFFFFF;
        }
        [data-testid="stButton"] button:not([kind="primary"]):hover,
        [data-testid="stDownloadButton"] button:hover {
            background: #F8E3EA;
            border-color: #B24C63;
            color: #5A1A32;
        }
        [data-testid="stRadio"] [aria-checked="true"] > div:first-child {
            background-color: #B24C63;
            border-color: #B24C63;
        }
        [data-testid="stTabs"] button[aria-selected="true"] {
            color: #5A1A32;
            border-bottom-color: #B24C63;
        }
        </style>
        """,
        unsafe_allow_html=True
    )


def apply_portal_selection_theme():
    st.markdown(
        """
        <style>
        [data-testid="stAppViewContainer"]:has(.portal-selection-page) {
            background:
                radial-gradient(ellipse at 18% 12%, rgba(178, 76, 99, 0.25), transparent 34%),
                radial-gradient(ellipse at 86% 82%, rgba(90, 26, 50, 0.6), transparent 42%),
                linear-gradient(135deg, #170F1A 0%, #241321 48%, #351628 100%);
            color: #FFF7FA;
        }
        [data-testid="stAppViewContainer"]:has(.portal-selection-page) [data-testid="stHeader"] {
            background: rgba(23, 15, 26, 0.72);
        }
        [data-testid="stAppViewContainer"]:has(.portal-selection-page) [data-testid="stSidebar"] {
            display: none;
        }
        [data-testid="stAppViewContainer"]:has(.portal-selection-page) [data-testid="stMainBlockContainer"] {
            padding-top: 2.5rem !important;
            padding-bottom: 1rem !important;
        }
        [data-testid="stAppViewContainer"]:has(.portal-selection-page) .portal-selection-page {
            display: none;
        }
        [data-testid="stAppViewContainer"]:has(.portal-selection-page) .portal-hero {
            max-width: 760px;
            margin: 0.25rem auto 0.2rem;
            text-align: center;
        }
        [data-testid="stAppViewContainer"]:has(.portal-selection-page) .portal-eyebrow,
        [data-testid="stAppViewContainer"]:has(.portal-selection-page) .portal-card-kicker {
            color: #E5A6B5;
            font-size: 0.76rem;
            font-weight: 700;
            letter-spacing: 0.18em;
        }
        [data-testid="stAppViewContainer"]:has(.portal-selection-page) .portal-hero h1 {
            color: #FFF7FA;
            font-size: clamp(2.3rem, 4.5vw, 3.5rem);
            letter-spacing: -0.045em;
            margin: 0.2rem 0 0.45rem;
        }
        [data-testid="stAppViewContainer"]:has(.portal-selection-page) .portal-hero > p:last-child {
            color: #E8D5DE;
            font-size: 0.98rem;
        }
        [data-testid="stAppViewContainer"]:has(.portal-selection-page) .portal-card-content {
            min-height: 220px;
            padding: 1.1rem;
            background: linear-gradient(150deg, rgba(61, 35, 54, 0.96), rgba(37, 23, 39, 0.97)) !important;
            border: 1px solid rgba(229, 166, 181, 0.3) !important;
            border-radius: 22px;
            box-shadow: 0 22px 55px rgba(8, 4, 12, 0.28);
            transition: transform 160ms ease, border-color 160ms ease, box-shadow 160ms ease;
        }
        [data-testid="stAppViewContainer"]:has(.portal-selection-page) .portal-card-content:hover {
            border-color: rgba(229, 166, 181, 0.72) !important;
            box-shadow: 0 26px 65px rgba(8, 4, 12, 0.4);
            transform: translateY(-5px);
        }
        [data-testid="stAppViewContainer"]:has(.portal-selection-page) .portal-card-icon {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            width: 3rem;
            height: 3rem;
            border: 1px solid rgba(229, 166, 181, 0.28);
            border-radius: 16px;
            background: rgba(178, 76, 99, 0.2);
            font-size: 1.65rem;
        }
        [data-testid="stAppViewContainer"]:has(.portal-selection-page) .portal-card-kicker {
            margin: 0.7rem 0 0.35rem;
            font-size: 0.68rem;
        }
        [data-testid="stAppViewContainer"]:has(.portal-selection-page) .portal-card-content h2 {
            color: #FFF7FA;
            font-size: 1.55rem;
            margin: 0 0 0.3rem;
        }
        [data-testid="stAppViewContainer"]:has(.portal-selection-page) .portal-card-content > p:last-child {
            color: #E4D4DD;
            font-size: 0.9rem;
            line-height: 1.35;
            margin-bottom: 0.3rem;
            min-height: 3rem;
        }
        [data-testid="stAppViewContainer"]:has(.portal-selection-page) [class*="st-key-enter_student_portal"] button,
        [data-testid="stAppViewContainer"]:has(.portal-selection-page) [class*="st-key-enter_faculty_portal"] button {
            min-height: 3rem;
            border: 1px solid #D1788D;
            border-radius: 11px;
            background: linear-gradient(105deg, #8D3B55, #B24C63);
            color: #FFFFFF;
            font-weight: 650;
            transition: filter 140ms ease, transform 140ms ease;
        }
        [data-testid="stAppViewContainer"]:has(.portal-selection-page) [class*="st-key-enter_student_portal"] button:hover,
        [data-testid="stAppViewContainer"]:has(.portal-selection-page) [class*="st-key-enter_faculty_portal"] button:hover {
            border-color: #E5A6B5;
            background: linear-gradient(105deg, #A34B66, #C45D76);
            color: #FFFFFF;
            filter: brightness(1.08);
            transform: translateY(-1px);
        }
        [data-testid="stAppViewContainer"]:has(.portal-selection-page) [class*="st-key-enter_student_portal"] button p,
        [data-testid="stAppViewContainer"]:has(.portal-selection-page) [class*="st-key-enter_faculty_portal"] button p {
            color: #FFFFFF !important;
        }
        @media (max-width: 768px) {
            [data-testid="stAppViewContainer"]:has(.portal-selection-page) .portal-hero {
                margin: 1rem auto 1.5rem;
            }
            [data-testid="stAppViewContainer"]:has(.portal-selection-page) .portal-card-content {
                min-height: auto;
            }
        }
        </style>
        """,
        unsafe_allow_html=True
    )
