# Parcial-AREP-1
## AREP
## Emily Noreña Cardozo

## Arquitectura

El cliente hace una solicitud GET al servidor fachada y este la pasa al servicio backend. La fachada y el backend se ejecutan en máquinas virtuales independientes.
El backend tiene toda lógica de procesamiento de parámetros y retorna un JSON con la solicitud respectiva del cliente para una llave-valor (set o get).

## Para ejecutar:

1. Clonar el repositorio
2. `cd Parcial-AREP-1`
3. Ejecutar `mvn clean install`
4. Ejecutar el servidor backend: `java -cp target\classes edu.eci.escuelaing.back.Backend`
5. Ejecutar el servidor backend: `java -cp target\classes edu.eci.escuelaing.facade.HttpServerFacade`
6. En el browser buscar: `http://localhost:36000/`
7. Método **GET /setkv?key={key}&value={value}**
8. Método **GET /getkv?key={key}**
9. Error 404
   
