from flask import Flask, render_template

app = Flask(__name__)


@app.route('/')
def index():
    return render_template('index.html')

@app.route('/signup/donor')
def signup_donor():
    return render_template('signup_donor.html')\

@app.route('/login')
def login():
    return render_template('login.html')