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
    ]
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


def courses_for(department, semester):
    if semester in COMMON_COURSES:
        return COMMON_COURSES[semester]
    if department == "IT":
        return IT_COURSES[semester]
    return [(None, name) for name in SUGGESTED_DEPARTMENT_COURSES[department][semester]]
