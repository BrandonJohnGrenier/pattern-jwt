import React from "react";
import ManageAuthorityDialog from "./ManageAuthorityDialog.jsx";
import DeleteAuthorityDialog from "./DeleteAuthorityDialog.jsx";

const Timestamp = require("react-timestamp");

class AuthorityTable extends React.Component {
    
    render() {
        return (
            <div>
                <ManageAuthorityDialog ref="manageAuthorityDialog" authorityUpdated={this.props.authorityUpdated} /> 
                <DeleteAuthorityDialog ref="deleteAuthorityDialog" authorityDeleted={this.props.authorityDeleted} />
                
                <table className="display-table">
                  <thead>
                    <tr>
                      <th className="dth left-pad-0">Authority</th>
                      <th className="dth">ID</th>
                      <th className="dth">Created</th>
                      <th className="dth right-pad-0"></th>
                    </tr>
                  </thead>
                  
                  <tbody>
                    {this.props.authorities.map((authority) => 
                     <tr key={authority.id}>
                       <td className="dtr left-pad-0">
                         {authority.name}<br/>
                         <span className="description">{authority.description}</span>
                       </td>
                       <td className="dtr">{authority.id}</td>
                       <td className="dtr"><Timestamp time={authority.created/1000} format="full" /></td>
                       <td className="dtr right-pad-0"> 
                         <i className="fa fa-times inline-button" onClick={() => this.refs.deleteAuthorityDialog.show(authority)}></i>
                         <i className="fa fa-pencil inline-button" style={{marginRight:"5px"}} onClick={() => this.refs.manageAuthorityDialog.show(authority)}></i> 
                       </td>
                     </tr>
                    )}
                  </tbody>
                </table>
            </div>    
        );
    }
  
}

export default AuthorityTable;