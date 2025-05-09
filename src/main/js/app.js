'use strict';


import { render } from 'react-dom';
import React from 'react';
import { Jumbotron, Container, Button, Form, Col, Row, Navbar } from 'react-bootstrap';


// end::vars[]





class App extends React.Component { // <1>

	constructor(props) {
		super(props);
		this.handleSubmit = this.handleSubmit.bind(this);
		this.onSend = this.onSend.bind(this);
		this.handleInput = this.handleInput.bind(this);
		this.handleQmChange = this.handleQmChange.bind(this);
		this.state = {
			qm: "",
			queue:"",
			result:"",
			body:"",
			type:"",
			qmlist:{},
			currentqueues:[]
		}
	};

	
	



	handleSubmit(e) {
		e.preventDefault();
		const mip = {};
		mip.qm     = this.state.qm;
		mip.body   = this.state.body;
		mip.header = this.state.header;
		mip.queue  = this.state.queue;
		mip.queue  = this.state.queue;
		mip.type = this.state.type;

		this.onSend(mip);
		//window.location = "#";
	}

	handleInput = (evt) => {
		this.setState({
			[evt.target.name]: evt.target.value 
		});
	}

	handleQmChange = (evt) => {
		this.setState({
			[evt.target.name]: evt.target.value ,
			currentqueues: this.state.qmlist[evt.target.value]
		});
		
	}
	


	onSend(mip) {
		
		
		fetch('/mip/sendx', {
			method: 'POST',
			body: JSON.stringify(mip),
			timeout: 4000,
			headers: {
				'Content-Type': 'application/json'
			}
		})
		.then(function(response) {
			return response.text();
		}).then(data => {
			//console.log(data); 
			let newline = "";
			if (this.state.result != "") newline ='\n';
			this.setState({ result:  `${this.state.result}${newline}${data}` });
		});
	

	}

	componentDidMount() { // <2>
		fetch('/miui/load', {
			method: 'GET',
			timeout: 4000,
			headers: {
				'Content-Type': 'application/json'
			}
		})
		.then((response) => 
			response.json())
    	.then((qms)=>{
			  this.setState({qmlist : qms.qmanagers });
		});
		
	}

	render() { // <3>
		let qmgrs = Object.keys(this.state.qmlist);
		let qmOptionItems = Object.keys(this.state.qmlist).map(qm => 
			<option key={qm}>{qm}</option>
		);
		let queueOptionItems = this.state.currentqueues.map(t => 
			<option key={t}>{t}</option>
			
        );
		
		return (
			<React.Fragment>
				<Container className="p-3">
					<Jumbotron>
						<Navbar bg="dark" variant="dark">
							<Navbar.Brand href="/">
								Message Injector
							</Navbar.Brand>
						</Navbar>
						<br />
						<br />
						<Form onSubmit={this.handleSubmit} action="/miui/sendxx">
							<Row>
								<Col>
								<Form.Row>
									<Col>
										<Form.Group controlId="Mip_Qm1">
										<Form.Label>Queue Manager</Form.Label>
										<Form.Control required as="select"   value={this.state.qm} 
										 name="qm" onChange={this.handleQmChange} >
										 <option value=""></option>
										 {qmOptionItems}
										</Form.Control>
										</Form.Group>
									</Col>
								</Form.Row>
								<Form.Row>
									<Col>
										<Form.Group controlId="Mip_Queue1">
										<Form.Label>Queue Name</Form.Label>
										<Form.Control  required as="select"  name="queue" value={this.state.queue} 
										onChange={this.handleInput} >
										<option value=""></option>
										 {queueOptionItems}
										 </Form.Control>
										</Form.Group>
									</Col>
								</Form.Row>	
								<br />
								<br />
								<Row>
									  
										<Col>
											<Button  size="lg" block type="submit" value="Submit">
												Send
											</Button>
										</Col>
								</Row>
								<br />
								<Row>
										<Col>
											<Form.Group controlId="Mip_response">
												<Form.Label>Response</Form.Label>
												<Form.Control as="textarea"  rows={4} readOnly value={this.state.result}
												onChange={this.handleInput} 
												/>
											</Form.Group>
										</Col>  
									</Row>
								</Col>
								<Col>
									<Row>
										<Col>
											<Form.Group controlId="Mip_MessageType">
												<Form.Label>MessageType</Form.Label>
												<Form.Control  as="select"  value={this.state.type} 
												name="type" onChange={this.handleInput} >
												<option value="TextMessage">TextMessage</option>
												<option value="MessageSwitch">MessageSwitch</option>
											</Form.Control>
											</Form.Group>
										</Col>
									</Row>
									<Row>
										<Col>
											<Form.Group controlId="Mip_Body">
												<Form.Label>Body</Form.Label>
												<Form.Control required as="textarea" rows={16}  value={this.state.body} 
												name="body" onChange={this.handleInput} />
											</Form.Group>
										</Col>
									</Row>
									
								</Col>
							</Row>
					</Form>
					</Jumbotron>
				</Container>
			</React.Fragment>

		)
	}
}
// end::app[]




// tag::render[]
render(
	<App />,
	document.getElementById('react')
)
// end::render[]
