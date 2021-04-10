from flask import Flask, render_template, url_for, request, redirect
from datetime import datetime
from flask_sqlalchemy import SQLAlchemy


app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///test.db'
db = SQLAlchemy(app)


class Todo(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    item = db.Column(db.String(200), nullable=False) #don't leave empty
    number = db.Column(db.Integer, primary_key=True)
    date_created = db.Column(db.DateTime, default=datetime.utcnow)

    def __repr__(self):
        return '<Items &r>' % self.id


@app.route('/', methods=['POST', 'GET'])
def index():
    if request.method == 'POST':
        item_content = request.form['item']
        new_item = Todo(item=item_content)

        try: 
            db.session.add()
            db.session.commit()
            return redirect('/')
        except:
            return "There's an issue adding your item"
    else:
        items = Todo.query.order_by(Todo.date_created).all()
        return render_template('index.html', items=items)
@app.route('/delete/<int:id>')
def delete(id):
    item_to_delete = Todo.query.get_or_404(id)

    try:
        db.session.delete(item_to_delete)
        db.session.commit()
        return redirect('/')
    except:
        return "There's a problem deleting that item"

@app.route('/update/<int:id>', methods=['GET', 'POST'])
def update(id):
    item = Todo.query.get_or_404(id)
    if request.method == 'POST':
        item.content = request.form['item']

        try:
            db.session.commit()
            return redirect('/')
        except:
            return "There was an issue updating your items"
    else:
        return render_template('update.html', item=item)

if __name__ == "__main__":
    app.run(debug=True)

