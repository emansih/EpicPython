from flask import Flask, render_template, request

import firebase_admin
from firebase_admin import credentials
from firebase_admin import firestore
import time

app = Flask(__name__)
cred = credentials.Certificate('key.json')
firebase_admin.initialize_app(cred)

db = firestore.client()


@app.route('/')
def index():
    return render_template('index.html')


@app.route('/signup/donor')
def signup_donor():
    return signup('donor')


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


@app.route('/storeFood', methods=['POST'])
def storefood():
    dietary = request.form['category']
    foodname = request.form['foodName']
    quantity = request.form['quantity']
    expirydate = request.form['expiryDate']
    description = request.form['description']
    uid = request.cookies.get('userId')

    doc_ref = db.collection(u'food')
    doc_ref.add({
        u'description': foodname,
        u'dietary': dietary,
        u'amount': quantity,
        u'dateExpire': expirydate,
        u'timestamp': int(time.time()),
        u'additionalNotes': description,
        u'uid': uid
    })
    return render_template('index.html')


@app.route('/cart')
def cart():
    return render_template('cart.html')


@app.route('/list')
def list():
    return render_template('donation_received.html')


@app.route('/charity')
def charity():
    return render_template('charity.html')
