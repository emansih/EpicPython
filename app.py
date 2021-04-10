from flask import Flask, render_template, request,redirect

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

@app.route('/authCheck')
def authCheck():
    doc_ref = db.collection(u'users')
    uid = request.cookies.get('userId')
    query = doc_ref.where(u'uid', u'==', uid).get()
    return render_template('registrationForm.html') if not query else render_template('index.html')

@app.route('/storeUserData', methods=['POST'])
def storeUserData():
    userType = request.form['userType']
    name = request.form['name']
    phoneNumber = request.form['phoneNumber']
    divLatitude = request.form['divLatitude']
    divLongitude  = request.form['divLongitude']
    geoHash  = request.form['geoHash']
    uid = request.cookies.get('userId')
    doc_ref = db.collection(u'users')
    doc_ref.add({
        u'name': name,
        u'phoneNumber': phoneNumber,
        u'uid': uid,
        u'userType': userType,
        u'latitude': divLatitude,
        u'longitude': divLatitude,
        u'geohash': geoHash
    })
    return redirect("/")


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
    return redirect("/")


@app.route('/cart')
def cart():
    return render_template('cart.html')


@app.route('/list')
def list():
    return render_template('donation_received.html')


@app.route('/charity')
def charity():
    return render_template('charity.html')
