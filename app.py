from flask import Flask, render_template

app = Flask(__name__)


@app.route('/')
def index():
    return render_template('index.html')


@app.route('/signup/donor')
def signup_donor():
    return signup('donor')


@app.route('/signup/buyer')
def signup_buyer():
    return signup('buyer')


@app.route('/signup/free')
def signup_free():
    return signup('free')


def signup(type):
    return render_template('signup.html', user_type=type)


@app.route('/login')
def login():
    return render_template('login.html')

@app.route('/donate')
def donate():
    return render_template('donate.html')

@app.route('/cart')
def cart():
    return render_template('cart.html')
